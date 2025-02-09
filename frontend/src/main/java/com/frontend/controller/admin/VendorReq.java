package com.frontend.controller.admin;

import java.util.Set;

import com.frontend.entity.store.Store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorReq {

	private String name;
	private String contactInfo;
	private Set<Store> store;
}
