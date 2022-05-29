package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StorePayment {

	private final int id;
	private final int orderId;
	private final int gatewayId;
	private final String transaction;
	private final String amount;
	private final String currency;
	private final String fee;
	private final PaymentStatus status;
	private final Date creationDate;
	private final Date lastUpdateDate;
	private final StoreCustomer payingCustomer;
	private final StoreCustomer receivingCustomer;
	private final List<StorePaymentProduct> products;

	StorePayment(NamelessAPI api, JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.orderId = json.get("order_id").getAsInt();
		this.gatewayId = json.get("gateway_id").getAsInt();
		this.transaction = json.get("transaction").getAsString();
		this.amount = json.get("amount").getAsString();
		this.currency = json.get("currency").getAsString();
		this.fee = json.get("fee").getAsString();
		this.status = PaymentStatus.BY_ID[json.get("status_id").getAsInt()];
		this.creationDate = new Date(json.get("created").getAsLong() * 1000);
		this.lastUpdateDate = new Date(json.get("last_updated").getAsLong() * 1000);
		this.payingCustomer = new StoreCustomer(api, json.getAsJsonObject("customer"));
		this.receivingCustomer = new StoreCustomer(api, json.getAsJsonObject("recipient"));

		JsonArray productsJson = json.getAsJsonArray("products");
		this.products = new ArrayList<>(productsJson.size());
		for (JsonElement element : productsJson) {
			this.products.add(new StorePaymentProduct(element.getAsJsonObject()));
		}
	}

	public int id() {
		return id;
	}

	public int orderId() {
		return orderId;
	}

	public int gatewayId() {
		return gatewayId;
	}

	public String transaction() {
		return transaction;
	}

	public String amount() {
		return amount;
	}

	public String currency() {
		return currency;
	}

	public String fee() {
		return fee;
	}

	public PaymentStatus status() {
		return status;
	}

	public Date creationDate() {
		return creationDate;
	}

	public Date lastUpdatedDate() {
		return lastUpdateDate;
	}

	public StoreCustomer payingCustomer() {
		return payingCustomer;
	}

	public StoreCustomer receivingCustomer() {
		return receivingCustomer;
	}

	public List<StorePaymentProduct> products() {
		return products;
	}

}
