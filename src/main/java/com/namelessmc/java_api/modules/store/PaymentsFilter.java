package com.namelessmc.java_api.modules.store;

import java.util.Objects;

public class PaymentsFilter {

	private final String name;
	private final String value;

	private PaymentsFilter(String name, String value) {
		this.name = Objects.requireNonNull(name);
		this.value = Objects.requireNonNull(value);
	}

	public String name() {
		return this.name;
	}

	public String value() {
		return this.value;
	}

	public static PaymentsFilter order(int orderId) {
		return new PaymentsFilter("order", String.valueOf(orderId));
	}

	public static PaymentsFilter gateway(int gatewayId) {
		return new PaymentsFilter("gateway", String.valueOf(gatewayId));
	}

	public static PaymentsFilter status(PaymentStatus status) {
		return new PaymentsFilter("status", String.valueOf(status.ordinal()));
	}

	public static PaymentsFilter payingCustomer(int customerId) {
		return new PaymentsFilter("customer", String.valueOf(customerId));
	}

	public static PaymentsFilter payingCustomer(StoreCustomer customer) {
		return payingCustomer(customer.id());
	}

	public static PaymentsFilter receivingCustomer(int customerId) {
		return new PaymentsFilter("recipient", String.valueOf(customerId));
	}

	public static PaymentsFilter receivingCustomer(StoreCustomer customer) {
		return payingCustomer(customer.id());
	}

	public static PaymentsFilter limit(int limit) {
		return new PaymentsFilter("limit", String.valueOf(limit));
	}

}
