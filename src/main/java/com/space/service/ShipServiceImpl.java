package com.space.service;

import com.space.exception.BadRequestException;
import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ShipServiceImpl implements ShipService {

	private final ShipRepository shipRepository;

	@Autowired
	public ShipServiceImpl(ShipRepository shipRepository) {
		this.shipRepository = shipRepository;
	}

	@Override
	public Page<Ship> getAllShips(Specification<Ship> specification, Pageable pageable) {
		return shipRepository.findAll(specification, pageable);
	}

	@Override
	public Specification<Ship> filterByName(String name) {
		return (root, query, builder) -> name == null ? null : builder.like(root.get("name"), "%" + name + "%");
	}

	@Override
	public Specification<Ship> filterByPlanet(String planet) {
		return (root, query, builder) -> planet == null ? null : builder.like(root.get("planet"), "%" + planet + "%");
	}

	@Override
	public Specification<Ship> filterByUsage(Boolean isUsed) {
		return (root, query, builder) -> {

			if (isUsed == null) return null;

			if (isUsed) return builder.isTrue(root.get("isUsed"));

			return builder.isFalse(root.get("isUsed"));
		};
	}

	@Override
	public Specification<Ship> filterByShipType(ShipType shipType) {
		return (root, query, builder) -> shipType == null ? null : builder.equal(root.get("shipType"), shipType);
	}

	@Override
	public Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
		return (root, query, builder) -> {
			if (minCrewSize == null && maxCrewSize == null) return null;

			if (minCrewSize == null) return builder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);

			if (maxCrewSize == null) return builder.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize);

			return builder.between(root.get("crewSize"), minCrewSize, maxCrewSize);
		};
	}

	@Override
	public Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
		return (root, query, builder) -> {
			if (minSpeed == null && maxSpeed == null) return null;

			if (minSpeed == null) return builder.lessThanOrEqualTo(root.get("speed"), maxSpeed);

			if (maxSpeed == null) return builder.greaterThanOrEqualTo(root.get("speed"), minSpeed);

			return builder.between(root.get("speed"), minSpeed, maxSpeed);
		};
	}

	@Override
	public Specification<Ship> filterByDate(Long after, Long before) {
		return (root, query, builder) -> {
			if (after == null && before == null)
				return null;

			if (after == null) {
				Date beforeDate = new Date(before);
				return builder.lessThanOrEqualTo(root.get("prodDate"), beforeDate);
			}

			if (before == null) {
				Date afterDate = new Date(after);
				return builder.greaterThanOrEqualTo(root.get("prodDate"), afterDate);
			}

			Date beforeDate = new Date(before);
			Date afterDate = new Date(after);

			return builder.between(root.get("prodDate"), afterDate, beforeDate);
		};
	}

	@Override
	public Specification<Ship> filterByRating(Double minRating, Double maxRating) {
		return (root, query, builder) -> {
			if (minRating == null && maxRating == null) return null;

			if (minRating == null) return builder.lessThanOrEqualTo(root.get("rating"), maxRating);

			if (maxRating == null) return builder.greaterThanOrEqualTo(root.get("rating"), minRating);

			return builder.between(root.get("rating"), minRating, maxRating);
		};
	}

	@Override
	@Transactional
	public Ship createShip(Ship ship) {

		if (ship.getName() == null ||
						ship.getPlanet() == null ||
						ship.getShipType() == null ||
						ship.getProdDate() == null ||
						ship.getSpeed() == null ||
						ship.getCrewSize() == null ||
						ship.getName().length() > 50 || ship.getName().isEmpty() ||
						ship.getPlanet().length() > 50 || ship.getPlanet().isEmpty() ||
						ship.getSpeed() < 0.01 || ship.getSpeed() > 0.99 ||
						ship.getCrewSize() <= 0 || ship.getCrewSize() > 9999 ||
						ship.getProdDate()
								.toInstant()
								.atZone(ZoneId.systemDefault())
								.toLocalDate()
								.getYear() < 2800 ||
						ship.getProdDate()
								.toInstant()
								.atZone(ZoneId.systemDefault())
								.toLocalDate()
								.getYear() > 3019
		) throw new BadRequestException();

		else if (ship.getUsed() == null) {
			ship.setUsed(false);
		}

		ship.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);

		ship.setRating(countRating(ship));

		return shipRepository.saveAndFlush(ship);
	}

	@Override
	@Transactional
	public Ship getShipById(Long id) {
		if (isNotValid(id)) throw new BadRequestException();

		return shipRepository.findById(id)
												 .orElse(null);
	}

	@Override
	@Transactional
	public Ship updateShip(Ship ship, Long id) {

		Ship tmpShip = getShipById(id);

		if (tmpShip == null) throw new NotFoundException();

		if (ship.getName() != null) {

			if (ship.getName()
							.length() <= 50 && !ship.getName()
																			.isEmpty())
				tmpShip.setName(ship.getName());
			else throw new BadRequestException();
		}

		if (ship.getPlanet() != null) {

			if (ship.getPlanet()
							.length() <= 50 && !ship.getPlanet()
																			.isEmpty()) {
				tmpShip.setPlanet(ship.getPlanet());
			} else throw new BadRequestException();
		}

		if (ship.getShipType() != null) tmpShip.setShipType(ship.getShipType());

		if (ship.getProdDate() != null) {

			if (ship.getProdDate()
							.toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDate()
							.getYear() >= 2800 &&
							ship.getProdDate()
									.toInstant()
									.atZone(ZoneId.systemDefault())
									.toLocalDate()
									.getYear() <= 3019) {
				tmpShip.setProdDate(ship.getProdDate());
			} else throw new BadRequestException();
		}

		if (ship.getUsed() != null) tmpShip.setUsed(ship.getUsed());

		if (ship.getSpeed() != null) {

			if (ship.getSpeed() >= 0.01 && ship.getSpeed() <= 0.99) {

				tmpShip.setSpeed(
								(double) Math.round(ship.getSpeed() * 100) / 100
				);
			} else throw new BadRequestException();
		}

		if (ship.getCrewSize() != null) {

			if (ship.getCrewSize() > 0 && ship.getCrewSize() <= 9999) {
				tmpShip.setCrewSize(ship.getCrewSize());
			} else throw new BadRequestException();
		}

		tmpShip.setId(id);
		tmpShip.setRating(countRating(tmpShip));

		return shipRepository.save(tmpShip);
	}

	@Override
	@Transactional
	public void deleteShip(Long id) {
		if (isNotValid(id)) throw new BadRequestException();

		if (!shipRepository.existsById(id)) {
			throw new NotFoundException();
		}

		shipRepository.deleteById(id);
	}

	@Override
	@Transactional
	public List<Ship> getFilteredShipsList(String name, String planet, ShipType shipType, Long after, Long before,
																				 Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
																				 Integer maxCrewSize, Double minRating, Double maxRating) {

		List<Ship> ships = shipRepository.findAll();

		if (name != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getName()
																			 .contains(name))
									 .collect(Collectors.toList());
		}

		if (planet != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getPlanet()
																			 .contains(planet))
									 .collect(Collectors.toList());
		}

		if (shipType != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getShipType()
																			 .equals(shipType))
									 .collect(Collectors.toList());
		}

		if (after != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getProdDate()
																			 .after(new Date(after)))
									 .collect(Collectors.toList());
		}

		if (before != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getProdDate()
																			 .before(new Date(before)))
									 .collect(Collectors.toList());
		}

		if (isUsed != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getUsed()
																			 .equals(isUsed))
									 .collect(Collectors.toList());
		}

		if (minSpeed != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getSpeed() >= minSpeed)
									 .collect(Collectors.toList());
		}

		if (maxSpeed != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getSpeed() <= maxSpeed)
									 .collect(Collectors.toList());
		}

		if (minCrewSize != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getCrewSize() >= minCrewSize)
									 .collect(Collectors.toList());
		}

		if (maxCrewSize != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getCrewSize() <= maxCrewSize)
									 .collect(Collectors.toList());
		}

		if (minRating != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getRating() >= minRating)
									 .collect(Collectors.toList());
		}

		if (maxRating != null) {
			ships = ships.stream()
									 .filter(ship -> ship.getRating() <= maxRating)
									 .collect(Collectors.toList());
		}

		return ships;
	}

	private boolean isNotValid(Long id) {
		return id == null || id <= 0 || !Pattern.matches("\\d+", String.valueOf(id));
	}

	private Double countRating(Ship ship) {
		Double shipSpeed = ship.getSpeed();
		Double usedRate = ship.getUsed() ? 0.5d : 1.0d;
		int prodDate = ship.getProdDate()
											 .toInstant()
											 .atZone(ZoneId.systemDefault())
											 .toLocalDate()
											 .getYear();
		int currentYear = 3019;

		Double rating = (80.0 * shipSpeed * usedRate) / (double) (currentYear - prodDate + 1);

		return (double) Math.round(rating * 100) / 100;
	}
}
