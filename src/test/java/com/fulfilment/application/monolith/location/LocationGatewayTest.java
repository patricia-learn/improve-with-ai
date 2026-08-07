package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("NONEXISTENT-001");

    // then
    assertNull(location);
  }

  @Test
  public void testWhenResolveMultipleLocations() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when & then
    Location amsterdam = locationGateway.resolveByIdentifier("AMSTERDAM-001");
    assertNotNull(amsterdam);
    assertEquals("AMSTERDAM-001", amsterdam.identification);
    assertEquals(5, amsterdam.maxNumberOfWarehouses);
    assertEquals(100, amsterdam.maxCapacity);
  }
}
