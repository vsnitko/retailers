package com.itechart.retailers.service.impl;

import com.itechart.retailers.model.entity.*;
import com.itechart.retailers.repository.*;
import com.itechart.retailers.security.service.SecurityContextService;
import com.itechart.retailers.service.ApplicationService;
import com.itechart.retailers.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
	private final SecurityContextService securityContextService;
	private final ApplicationService applicationService;
	private final ApplicationRepository applicationRepository;
	private final ApplicationItemRepository applicationItemRepository;
	private final LocationItemRepository locationItemRepository;
	private final StateTaxRepository stateTaxRepository;
	private final CustomerCategoryRepository customerCategoryRepository;
	private final SecurityContextService securityService;

	@Override
	public boolean canAcceptApplication(Long applicationId) {
		Integer applicationOccupiedCapacity = applicationService.getOccupiedCapacity(applicationId);
		Integer locationAvailableCapacity = getCurrentAvailableCapacity();
		return applicationOccupiedCapacity <= locationAvailableCapacity;
	}

	@Override
	public Integer getCurrentAvailableCapacity() {
		int occupiedCapacity = 0;
		Location location = securityContextService.getCurrentLocation();
		for (LocationItem locationItem : location.getItemAssoc()) {
			occupiedCapacity += locationItem.getItem().getUnits() * locationItem.getAmount();
		}
		return location.getTotalCapacity() - occupiedCapacity;
	}

	@Override
	public void acceptApplication(Long applicationId) {
		Set<ApplicationItem> applicationItems = applicationRepository.getById(applicationId).getItemAssoc();
		Set<LocationItem> locationItems = applicationItems.stream()
				.map(this::createLocationItem)
				.collect(Collectors.toSet());
		for (LocationItem locationItem : locationItems) {
			Optional<LocationItem> locationItemDB =
					locationItemRepository.findLocationItemByItemAndLocation(locationItem.getItem(),
							locationItem.getLocation());
			if (locationItemDB.isPresent()) {
				if (locationItemDB.get().getCost() > locationItem.getCost()) {
					locationItem.setCost(locationItemDB.get().getCost());
					locationItem.setPrice(locationItemDB.get().getPrice());
					locationItem.setId(locationItemDB.get().getId());
				}
				locationItem.setAmount(locationItemDB.get().getAmount() + locationItem.getAmount());
				locationItemRepository.deleteById(locationItemDB.get().getId());
			}
			locationItemRepository.save(locationItem);
		}
		Application application = applicationService.getById(applicationId);
		application.setLastUpdDateTime(LocalDateTime.now());
		application.setLastUpdBy(securityContextService.getCurrentUser());
		application.setStatus("FINISHED_PROCESSING");
		applicationRepository.save(application);
		applicationItemRepository.deleteAll(application.getItemAssoc());
	}

	private LocationItem createLocationItem(ApplicationItem applicationItem) {
		if ("OFFLINE_SHOP".equals(applicationItem.getApplication().getDestLocation().getType())) {
			Long currentCustomerId = securityService.getCurrentCustomer().getId();
			Float rentalTaxRate = applicationItem.getApplication().getDestLocation().getRentalTaxRate();
			StateTax stateTax = stateTaxRepository.getByStateCode
					(StateCode.valueOf(applicationItem.getApplication().getDestLocation().getAddress().getStateCode()));
			CustomerCategory customerCategory = customerCategoryRepository.
					findByCustomerIdAndCategoryId(currentCustomerId, applicationItem.getItem().getCategory().getId()).get();
			Float categoryTaxRate = customerCategory.getCategoryTax();
			Float convertRate = 0.01f;
			Float itemPrice = applicationItem.getCost() * (1f + rentalTaxRate * convertRate
					+ stateTax.getTax() * convertRate
					+ categoryTaxRate * convertRate);
			return new LocationItem(applicationItem.getAmount(), applicationItem.getCost(),
					securityContextService.getCurrentLocation(), applicationItem.getItem(),
					itemPrice);
		} else {
			return new LocationItem(applicationItem.getAmount(), applicationItem.getCost(),
					securityContextService.getCurrentLocation(), applicationItem.getItem());
		}
	}
}
