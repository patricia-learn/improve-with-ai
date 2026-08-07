package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  @Inject LocationGateway locationGateway;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Find the existing warehouse by business unit code
    Warehouse existingWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existingWarehouse == null || existingWarehouse.archivedAt != null) {
      throw new WebApplicationException(
          "Warehouse with business unit code '" + newWarehouse.businessUnitCode + "' does not exist or is already archived.", 404);
    }

    // Location Validation - confirm that the new warehouse location is valid
    Location location = locationGateway.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new WebApplicationException(
          "Location '" + newWarehouse.location + "' is not valid.", 400);
    }

    // Capacity Accommodation - ensure the new warehouse's capacity can accommodate the stock
    if (newWarehouse.capacity < existingWarehouse.stock) {
      throw new WebApplicationException(
          "New warehouse capacity (" + newWarehouse.capacity + ") cannot accommodate existing stock ("
              + existingWarehouse.stock + ").", 400);
    }

    // Stock Matching - confirm that the stock of the new warehouse matches the stock of the previous warehouse
    if (!newWarehouse.stock.equals(existingWarehouse.stock)) {
      throw new WebApplicationException(
          "New warehouse stock (" + newWarehouse.stock + ") must match existing warehouse stock ("
              + existingWarehouse.stock + ").", 400);
    }

    // Capacity and Stock Validation for location
    if (newWarehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
          "Warehouse capacity (" + newWarehouse.capacity + ") exceeds maximum capacity ("
              + location.maxCapacity + ") for location '" + newWarehouse.location + "'.", 400);
    }

    // Archive the old warehouse
    existingWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(existingWarehouse);

    // Create the new warehouse
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
