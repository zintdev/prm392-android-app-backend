package com.example.backend.domain.enums;

public enum OrderStatus {
	PENDING,      // waiting for online payment confirmation or manual approval
	PAID,         // online payment captured successfully
	PROCESSING,   // confirmed and being prepared; default path for COD orders
	SHIPPED,      // handed off to the carrier for delivery
	COMPLETED,    // delivered successfully or picked up by the customer
	CANCELLED,    // cancelled before completion
	KEEPING       // reserved in store awaiting customer pickup
}