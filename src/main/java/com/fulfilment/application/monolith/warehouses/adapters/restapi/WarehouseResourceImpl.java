package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;

  @Inject private CreateWarehouseUseCase createWarehouseUseCase;

  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @Inject private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream()
        .filter(w -> w.archivedAt == null)
        .map(this::toWarehouseResponse)
        .toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    // Convert request to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock();

    // Call use case with validations
    createWarehouseUseCase.create(domainWarehouse);

    // Return the created warehouse
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found.", 404);
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found.", 404);
    }
    archiveWarehouseUseCase.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    // Convert request to domain model
    var newDomainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    newDomainWarehouse.businessUnitCode = businessUnitCode;
    newDomainWarehouse.location = data.getLocation();
    newDomainWarehouse.capacity = data.getCapacity();
    newDomainWarehouse.stock = data.getStock();

    // Call use case with validations
    replaceWarehouseUseCase.replace(newDomainWarehouse);

    // Return the new warehouse
    return toWarehouseResponse(newDomainWarehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
