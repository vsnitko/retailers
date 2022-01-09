package com.itechart.retailers.service.impl;

import com.itechart.retailers.model.entity.*;
import com.itechart.retailers.model.entity.projection.UserView;
import com.itechart.retailers.repository.*;
import com.itechart.retailers.security.service.SecurityContextService;
import com.itechart.retailers.service.AdminService;
import com.itechart.retailers.service.RoleService;
import com.itechart.retailers.service.exception.LocationIdentifierAlreadyExists;
import com.itechart.retailers.service.exception.LocationNotFoundException;
import com.itechart.retailers.service.exception.MailIsAlreadyInUse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SecurityContextService securityService;
    private final LocationRepository locationRepo;
    private final UserRepository userRepo;
    private final AddressRepository addressRepo;
    private final RoleService roleService;
    private final WarehouseRepository warehouseRepo;
    private final SupplierRepository supplierRepo;
    private final CustomerRepository customerRepo;
    private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public Location createLocation(Location location) throws LocationIdentifierAlreadyExists {
		addressRepo.save(location.getAddress());
		Customer customer = new Customer(securityService.getCurrentCustomerId());
		if (locationRepo.findLocationByIdentifierAndCustomerId(location.getIdentifier(), customer.getId()).isPresent()) {
			throw new LocationIdentifierAlreadyExists();
		}
		location.setCustomer(customer);
		return locationRepo.save(location);
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
	public List<UserView> getUsers() {
		return userRepo.findUserViewsByCustomerId(securityService.getCurrentCustomerId());
	}

    @Override
    @Transactional
    public User createUser(User user) throws LocationNotFoundException, MailIsAlreadyInUse {
        // TODO: Password generation
	    Customer customer = new Customer(securityService.getCurrentCustomerId());
	    user.setCustomer(customer);
	    if (userRepo.findUserByEmailAndCustomerId(user.getEmail(), user.getCustomer().getId()).isPresent()) {
		    throw new MailIsAlreadyInUse();
	    }
        user.setPassword(passwordEncoder.encode("1111"));
        user.setActive(true);
        user.setCustomer(new Customer(securityService.getCurrentCustomerId()));
        Long addressId = addressRepo.save(user.getAddress()).getId();
        user.setAddress(new Address(addressId));
        String roleStr = user.getRole().getRole();
        user.setRole(roleService.save(roleStr));
        if (roleStr.equals("DIRECTOR")) {
            user.setLocation(null);
        } else {
            String locationIdentifier = user.getLocation().getIdentifier();
            Location location = locationRepo.findLocationByIdentifier(locationIdentifier)
                    .orElseThrow(LocationNotFoundException::new);
            user.setLocation(new Location(location.getId()));
        }
        return userRepo.save(user);
    }

	@Override
	@Transactional
	public void updateUserStatus(Long id, boolean isActive) {
		userRepo.changeUserStatus(id, isActive);
	}

    @Override
    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        supplier = supplierRepo.save(supplier);
        Set<Warehouse> warehouses = supplier.getWarehouses();

        for (Warehouse wh : warehouses) {
            wh.setSupplier(supplier);
            addressRepo.save(wh.getAddress());
        }
        warehouseRepo.saveAll(warehouses);
        customerRepo.findById(securityService.getCurrentCustomerId()).get().getSuppliers().add(supplier);

        return supplier;
    }

	@Override
	public List<Supplier> findSuppliers() {
		return supplierRepo.findByCustomers_Id(securityService.getCurrentCustomerId());
	}

	@Override
	@Transactional
	public void updateSupplierStatus(Long id, boolean isActive) {
		supplierRepo.changeSupplierStatus(id, isActive);
	}
}
