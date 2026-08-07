package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  @Inject LocationGateway locationGateway;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Business Unit Code Verification - check if code already exists
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null && existing.archivedAt == null) {
      throw new WebApplicationException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' already exists.", 409);
    }

    // Location Validation - confirm that the warehouse location is valid
    Location location = locationGateway.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WebApplicationException(
          "Location '" + warehouse.location + "' is not valid.", 400);
    }

    // Warehouse Creation Feasibility - check if max warehouses reached at location
    long warehousesAtLocation = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(warehouse.location) && w.archivedAt == null)
        .count();
    if (warehousesAtLocation >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
          "Maximum number of warehouses (" + location.maxNumberOfWarehouses
              + ") already reached at location '" + warehouse.location + "'.", 400);
    }

    // Capacity and Stock Validation
    if (warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
          "Warehouse capacity (" + warehouse.capacity + ") exceeds maximum capacity ("
              + location.maxCapacity + ") for location '" + warehouse.location + "'.", 400);
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new WebApplicationException(
          "Stock (" + warehouse.stock + ") cannot exceed warehouse capacity ("
              + warehouse.capacity + ").", 400);
    }

    // if all went well, create the warehouse
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }
}
