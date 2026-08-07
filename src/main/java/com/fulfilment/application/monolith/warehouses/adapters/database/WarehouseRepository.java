package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse existing = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (existing != null) {
      existing.businessUnitCode = warehouse.businessUnitCode;
      existing.location = warehouse.location;
      existing.capacity = warehouse.capacity;
      existing.stock = warehouse.stock;
      existing.createdAt = warehouse.createdAt;
      existing.archivedAt = warehouse.archivedAt;
      this.persist(existing);
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse existing = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (existing != null) {
      this.delete(existing);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = this.find("businessUnitCode", buCode).firstResult();
    if (dbWarehouse != null) {
      return dbWarehouse.toWarehouse();
    }
    return null;
  }
}
