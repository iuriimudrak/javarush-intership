package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ShipService {

	Page<Ship> getAllShips(Specification<Ship> specification, Pageable pageable);

	Specification<Ship> filterByName(String name);
	Specification<Ship> filterByPlanet(String planet);
	Specification<Ship> filterByUsage(Boolean isUser);
	Specification<Ship> filterByShipType(ShipType shipType);
	Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize);
	Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed);
	Specification<Ship> filterByDate(Long after, Long before);
	Specification<Ship> filterByRating(Double minRating, Double maxRating);

	Ship createShip(Ship ship);
	Ship getShipById(Long id);
	Ship updateShip(Ship ship, Long id);
	void deleteShip(Long id);

	List<Ship> getFilteredShipsList(String name, String planet, ShipType shipType, Long after,
																	Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
																	Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating);
}
