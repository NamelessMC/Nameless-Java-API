package com.namelessmc.java_api.modules.store;

public enum PaymentStatus {

	PAYMENT_PENDING, // 0
	PAYMENT_COMPLETE, // 1
	PAYMENT_REFUNDED, // 2
	PAYMENT_CHARGED_BACK, // 3
	PAYMENT_DENIED, // 4

	;

	static final PaymentStatus[] BY_ID = PaymentStatus.values();

}
