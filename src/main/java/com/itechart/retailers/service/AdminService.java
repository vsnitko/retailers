package com.itechart.retailers.service;

import com.itechart.retailers.model.entity.Location;
import com.itechart.retailers.model.entity.Supplier;
import com.itechart.retailers.model.entity.User;
import com.itechart.retailers.model.entity.projection.UserView;

import java.util.List;
import java.util.Set;

public interface AdminService {

    boolean createLocation(Location location);

    void deleteLocation(Long id);

    void deleteLocations(Set<Long> ids);

    List<UserView> getUsers();

    boolean createUser(User user);

    void updateUserStatus(Long id, boolean isActive);

    boolean createSupplier(Supplier supplier);

    List<Supplier> findSuppliers();

    void updateSupplierStatus(Long id, boolean isActive);
}
