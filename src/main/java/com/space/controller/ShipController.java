package com.space.controller;

import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {

	private final ShipService shipService;

	@Autowired
	public ShipController(ShipService shipService) {
		this.shipService = shipService;
	}

	@GetMapping("/ships")
	public List<Ship> getAllShips(@RequestParam(required = false) String name,
																@RequestParam(required = false) String planet,
																@RequestParam(required = false) ShipType shipType,
																@RequestParam(required = false) Long after,
																@RequestParam(required = false) Long before,
																@RequestParam(required = false) Boolean isUsed,
																@RequestParam(required = false) Double minSpeed,
																@RequestParam(required = false) Double maxSpeed,
																@RequestParam(required = false) Integer minCrewSize,
																@RequestParam(required = false) Integer maxCrewSize,
																@RequestParam(required = false) Double minRating,
																@RequestParam(required = false) Double maxRating,
																@RequestParam(required = false, defaultValue = "ID") ShipOrder order,
																@RequestParam(required = false, defaultValue = "0") Integer pageNumber,
																@RequestParam(required = false, defaultValue = "3") Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

		return shipService.getAllShips(
						Specification.where(
										shipService.filterByName(name)
															 .and(shipService.filterByPlanet(planet))
															 .and(shipService.filterByUsage(isUsed))
															 .and(shipService.filterByShipType(shipType))
															 .and(shipService.filterBySpeed(minSpeed, maxSpeed))
															 .and(shipService.filterByCrewSize(minCrewSize, maxCrewSize))
															 .and(shipService.filterByDate(after, before))
															 .and(shipService.filterByRating(minRating, maxRating))), pageable)
											.getContent();
	}

	@GetMapping("/ships/count")
	public Integer getShipsCount(@RequestParam(required = false) String name,
															 @RequestParam(required = false) String planet,
															 @RequestParam(required = false) ShipType shipType,
															 @RequestParam(required = false) Long after,
															 @RequestParam(required = false) Long before,
															 @RequestParam(required = false) Boolean isUsed,
															 @RequestParam(required = false) Double minSpeed,
															 @RequestParam(required = false) Double maxSpeed,
															 @RequestParam(required = false) Integer minCrewSize,
															 @RequestParam(required = false) Integer maxCrewSize,
															 @RequestParam(required = false) Double minRating,
															 @RequestParam(required = false) Double maxRating) {
		return shipService.getFilteredShipsList(name, planet, shipType, after, before, isUsed,
																						minSpeed, maxSpeed, minCrewSize, maxCrewSize,
																						minRating, maxRating).size();
	}

	@PostMapping("/ships")
	@ResponseBody
	public Ship createShip(@RequestBody Ship ship) {
		return shipService.createShip(ship);
	}

	@GetMapping("/ships/{id}")
	public Ship getShipById(@PathVariable Long id) {

		Ship ship = shipService.getShipById(id);

		if (ship == null) throw new NotFoundException();

		return ship;
	}

	@PostMapping("/ships/{id}")
	public Ship updateShip(@RequestBody Ship ship, @PathVariable Long id) {
		return shipService.updateShip(ship, id);
	}

	@DeleteMapping("/ships/{id}")
	public void deleteShip(@PathVariable Long id) {
		shipService.deleteShip(id);
	}
}
