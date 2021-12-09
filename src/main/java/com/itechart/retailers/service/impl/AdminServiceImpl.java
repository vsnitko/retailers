package com.itechart.retailers.service.impl;

import com.itechart.retailers.model.entity.*;
import com.itechart.retailers.repository.*;
import com.itechart.retailers.repository.projection.UserView;
import com.itechart.retailers.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final LocationRepository locationRepo;
	private final CustomerRepository customerRepo;
	private final UserRepository userRepo;
	private final AddressRepository addressRepo;
	private final RoleRepository roleRepo;

	@Override
	public List<Location> findLocations(Long customerId) {
		return locationRepo.findLocationsByCustomerId(customerId);
	}

	@Override
	@Transactional
	public boolean createLocation(Location location, Long customerId) {
		addressRepo.save(location.getAddress());
		location.setCustomer(new Customer(customerId));

		return locationRepo.save(location).getId() != null;
	}

	@Override
	@Transactional
	public void deleteLocation(Long id) {
		locationRepo.deleteById(id);
	}

	@Override
	@Transactional
	public void deleteLocations(Set<Long> ids) {
		locationRepo.deleteAllByIdInBatch(ids);
	}

	@Override
	public List<UserView> findEmployees(Long customerId) {
		return userRepo.findUserViewsByCustomerId(customerId);
	}

	@Override
	@Transactional
	public boolean createUser(User user, Long customerId) {
		user.setCustomer(new Customer(customerId));

		Long addressId = addressRepo.save(user.getAddress()).getId();
		user.setAddress(new Address(addressId));

		String roleStr = user.getRole().getRole();
		user.setRole(roleRepo.getByRole(roleStr));

		if(roleStr.equals("DISPATCHER") || roleStr.equals("WAREHOUSE_MANAGER") || roleStr.equals("SHOP_MANAGER")){
			String locationIdentifier = user.getLocation().getIdentifier();

			user.setLocation(new Location(locationRepo.findLocationByIdentifier(locationIdentifier).get().getId()));
		}

		return userRepo.save(user).getId() != null;
	}

	@Override
	public Optional<Long> findCustomerId(String adminEmail) {
		return Optional.of(userRepo.findByEmail(adminEmail).get().getCustomer().getId());
	}
}
