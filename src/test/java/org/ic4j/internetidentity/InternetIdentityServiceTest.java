package org.ic4j.internetidentity;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.parser.IDLValue;
import org.ic4j.candid.types.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class InternetIdentityServiceTest {
	@Test
	public void prepareDelegationReturnsBeforeProxyCompletes() {
		CompletableFuture<IDLArgs> pending = new CompletableFuture<>();
		InternetIdentityService service = serviceWith(pending);

		CompletableFuture<PrepareDelegationResponse> response = Assertions.assertTimeoutPreemptively(
				Duration.ofSeconds(2),
				() -> service.prepareDelegation(42L, "https://example.com", new byte[] { 1 }, Optional.empty()));

		Assertions.assertFalse(response.isDone());
		pending.complete(delegationArgs());
		Assertions.assertArrayEquals(new byte[] { 1, 2, 3 }, response.join().userKey);
		Assertions.assertEquals(Long.valueOf(123L), response.join().timestamp);
	}

	@Test
	public void prepareDelegationMapsAlreadyCompletedResponse() {
		InternetIdentityService service = serviceWith(CompletableFuture.completedFuture(delegationArgs()));

		PrepareDelegationResponse response = service
				.prepareDelegation(42L, "https://example.com", new byte[] { 1 }, Optional.of(100L)).join();

		Assertions.assertArrayEquals(new byte[] { 1, 2, 3 }, response.userKey);
		Assertions.assertEquals(Long.valueOf(123L), response.timestamp);
	}

	@Test
	public void prepareDelegationPropagatesProxyFailure() {
		CompletableFuture<IDLArgs> pending = new CompletableFuture<>();
		InternetIdentityService service = serviceWith(pending);
		CompletableFuture<PrepareDelegationResponse> response = service
				.prepareDelegation(42L, "https://example.com", new byte[] { 1 }, Optional.empty());
		IllegalStateException failure = new IllegalStateException("Proxy failed");

		pending.completeExceptionally(failure);

		CompletionException exception = Assertions.assertThrows(CompletionException.class, response::join);
		Assertions.assertInstanceOf(InternetIdentityError.class, exception.getCause());
		Assertions.assertSame(failure, exception.getCause().getCause());
	}

	@Test
	public void prepareDelegationReportsMalformedResponseThroughFuture() {
		InternetIdentityService service = serviceWith(
				CompletableFuture.completedFuture(IDLArgs.create(Collections.emptyList())));

		CompletableFuture<PrepareDelegationResponse> response = service
				.prepareDelegation(42L, "https://example.com", new byte[] { 1 }, Optional.empty());

		CompletionException exception = Assertions.assertThrows(CompletionException.class, response::join);
		Assertions.assertInstanceOf(IndexOutOfBoundsException.class, exception.getCause());
	}

	private static IDLArgs delegationArgs() {
		return IDLArgs.create(Arrays.asList(
				IDLValue.create(new Byte[] { 1, 2, 3 }, Type.VEC),
				IDLValue.create(123L, Type.NAT64)));
	}

	private static InternetIdentityService serviceWith(CompletableFuture<IDLArgs> response) {
		InternetIdentityService service = new InternetIdentityService();
		service.internetIdentityProxy = (InternetIdentityProxy) Proxy.newProxyInstance(
				InternetIdentityProxy.class.getClassLoader(), new Class<?>[] { InternetIdentityProxy.class },
				(proxy, method, args) -> {
					if (!method.getName().equals("prepareDelegation"))
						throw new UnsupportedOperationException(method.getName());
					Assertions.assertEquals(42L, args[0]);
					Assertions.assertEquals("https://example.com", args[1]);
					Assertions.assertArrayEquals(new byte[] { 1 }, (byte[]) args[2]);
					return response;
				});
		return service;
	}
}
