package com.imprezer.controllers;

import com.imprezer.model.*;
import com.imprezer.repositories.EventDetailsRepository;
import com.imprezer.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class EventsProvider {

    private static Integer PAGE_SIZE = 10;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UsersProvider usersProvider;

    @Autowired
    private EventDetailsRepository eventDetailsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;


    @RequestMapping("/categories")
    public Object[] getCategories() {
        return Arrays.asList(EventCategory.values()).stream().map(el -> {
            Map map = new HashMap();
            map.put("name", el.get_nameUS());// TODO: make it user specific
            map.put("value", el.get_numeric());
            return map;
        }).toArray();
    }

    /**
     * Adds new event.
     *
     * @return added event
     */
    @RequestMapping(value = "/events", method = RequestMethod.POST)
    public void addEvent(@RequestBody FullEvent event, OAuth2Authentication principal) {
        if (principal == null) {
            throw new AccessDeniedException("Login to access your account");
        }
        String errorMessage = EventDataValidator.validateEvent(event);

        User u = usersProvider.addIfNotExistsAndGet(principal);

        if (errorMessage.isEmpty()) {
            persist(event, u, 300);
        } else {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private Event retrieveEvent(FullEvent event, String detailsId, int rank) {
        Event ev = new Event();
        ev.setName(event.getName());
        ev.setCategories(event.getCategories());
        ev.setBeginDateTime(event.getBeginDateTime());
        ev.setEndDateTime(event.getEndDateTime());
        ev.setDetailsId(detailsId);
        ev.setPlace(event.getPlace());
        ev.setRank(rank);
        return ev;
    }

    private EventDetails retrieveDetails(FullEvent event, String creatorId) {
        EventDetails details = new EventDetails();
        details.setLocation(event.getLocation());
        details.setAlcohol(event.isAlcohol());
        details.setCreatorId(creatorId);
        details.setDetails(event.getDetails());
        details.setPaid(event.isPaid());
        return details;
    }

    /**
     * Handling method for events-fetching request.
     *
     * @param scope    scope of events: best|all|filter
     * @param name     part of the name of an event
     * @param datetime date, when the event should occur
     * @param lat      place latitude
     * @param lng      place longitude
     * @param place    part of place name
     * @param range    range within [lat,lng]
     * @param inclCats included categories
     * @param exclCats excluded categories
     * @param paid     chargeable events (non-free)
     * @param alcohol  alcohol during event
     * @return list of events matching above params
     */
    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public List<Event> getEvents(@RequestParam(value = "scope") String scope,
                                 @RequestParam(value = "name", required = false) String name,
                                 //TODO: parse date and time as ZonedDateTime (as are in POST /events ...)
                                 //or parse it with dateformat
                                 @RequestParam(value = "datetime", required = false) String datetime,
                                 @RequestParam(value = "lat", required = false) Double lat,
                                 @RequestParam(value = "lng", required = false) Double lng,
                                 @RequestParam(value = "place", required = false) String place,
                                 @RequestParam(value = "range", required = false) Integer range,
                                 @RequestParam(value = "inclCats", required = false) List<Integer> inclCats,
                                 @RequestParam(value = "exclCats", required = false) List<Integer> exclCats,
                                 @RequestParam(value = "paid", required = false) Integer paid,
                                 @RequestParam(value = "alcohol", required = false) Integer alcohol,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 Pageable pageable) {

        //TODO apply params properly (take rank under consideration)
        switch (scope) {
            case "all":
                return eventRepository.findAll(pageable).getContent();
            case "filter":
                CriteriaBuilder eventCriteria = new CriteriaBuilder();
                CriteriaBuilder detailsCriteria = new CriteriaBuilder();
                List<EventDetails> details = new LinkedList<>();
                List<Event> result = new LinkedList<>();

                // DETAILS
                if (alcohol != null && alcohol != 0) {
                    detailsCriteria.withAlcohol(alcohol);
                }

                if (paid != null && paid != 0) {
                    detailsCriteria.paid(paid);
                }

                if (lat != null && lng != null) {
                    if (range != null) {
                        detailsCriteria.near2(lat, lng, range);
                    } else {
                        // TODO Consider searching with default range
                    }
                }

                if (detailsCriteria.getAmountOfAppliedFilters() > 0) {
                    // create query
                    Query q = Query.query(detailsCriteria.build());
                    // pagination
                    // paginate(page, q);

                    // result
                    try {
                        details = mongoTemplate.find(q, EventDetails.class);
                        result.addAll(details.stream().map(evDet -> eventRepository.findByDetailsId(evDet.getId()))
                                .collect(Collectors.toList()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // RAW EVENT
                if (name != null) {
                    eventCriteria.withName(name);
                }
                if (datetime != null) {
                    eventCriteria.onDateTime(datetime);
                }

                if (inclCats != null && !inclCats.isEmpty()) {
                    eventCriteria.includeOnlyCategories(inclCats);
                }

                if (exclCats != null && !exclCats.isEmpty()) {
                    eventCriteria.excludeCategories(exclCats);
                }

                if (place != null) {
                    eventCriteria.withPlace(place);
                }


                if (eventCriteria.getAmountOfAppliedFilters() > 0) {
                    if (details != null) {
                        for (EventDetails evDet : details) {
                            eventCriteria.andDetailsIdNotEqualTo(evDet.getId());
                        }
                    }
                    // create query
                    Query q = Query.query(eventCriteria.build());
                    // pagination
                    // paginate(page, q);

                    // result
                    try {
                        result.addAll(mongoTemplate.find(q, Event.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return result;
                }
            case "best":
                return eventRepository.findAllByOrderByRankDesc(pageable).getContent();
            default:
                throw new IllegalArgumentException("Scope value not matched. Possible scope values: [all|best|filter]");
        }
    }

    private void paginate(Integer page, Query q) {
        if (page != null) {
            if (page == 0) {
                q.limit(PAGE_SIZE);
            } else if (page > 0) {
                q.skip(page * PAGE_SIZE).limit(PAGE_SIZE);
            }
        }
    }

    /**
     * Event with id == eventId.
     *
     * @param eventId event id
     * @return list of events (for consistency, list.length == 0|1)
     */
    @RequestMapping("/events/{eventId}")
    public List<Event> getEvent(@PathVariable("eventId") String eventId) {
        //TODO return full "FullEvent" event
        return Arrays.asList(eventRepository.findById(eventId));
    }

    /**
     * Event (with id == eventId) details.
     *
     * @param eventId event id
     * @return list of events (for consistency, list.length == 0|1)
     */
    @RequestMapping("/events/{eventId}/details")
    public EventDetails getEventDetails(@PathVariable("eventId") String eventId) {
        Event ev = eventRepository.findById(eventId);
        if (ev == null)
            return null;
        return eventDetailsRepository.findById(ev.getDetailsId());
    }


    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    public void generate(OAuth2Authentication principal) {
        if (principal != null) {
            Random random = new Random();
            System.out.println("Generating data started");
            eventRepository.deleteAll();
            eventDetailsRepository.deleteAll();
            usersProvider.deleteAllUsers();
            mongoTemplate.indexOps(Event.class).ensureIndex(new GeospatialIndex("location"));


            final User u = usersProvider.addIfNotExistsAndGet(principal);

            int categoriesCount = EventCategory.values().length;
            IntStream.range(0, 100).forEach(i -> {
                FullEvent e = new FullEvent();
                e.setAlcohol(i % 2 == 0);
                e.setBeginDateTime(new Date(System.currentTimeMillis() - random.nextInt(100000000)));
                e.setCategories(Arrays.asList(EventCategory.ofValue(((i % categoriesCount) + 1) % categoriesCount),
                        EventCategory.ofValue((((i + 1) % categoriesCount) + 1) % categoriesCount)));
                e.setDetails("Best event ever, must come, must be. Edition " + i + ".");
                e.setLocation(new double[]{50 + ((random.nextDouble() - 0.5) * 20), 20 + ((random.nextDouble() - .5) * 20)});
                e.setEndDateTime(new Date(System.currentTimeMillis() + random.nextInt(100000000)));
                e.setName("Super awesome event name #" + i);
                e.setPaid(i % 2 != 0);
                e.setPlace("Krk, al. Mickiewicza " + i);
                e.setUser("Janusz" + i);
                if (principal != null) {
                    e.setCreatorId(u.getId());
                }
                persist(e, u, random.nextInt(100));
            });
            System.out.println("Generating data finished");
        } else {
            //TODO change to user ROLES
            throw new AccessDeniedException("Cannot generate while not being logged in.");
        }
    }

    private void persist(FullEvent event, User u, int rank) {
        EventDetails details = retrieveDetails(event, u.getId());
        eventDetailsRepository.insert(details);

        Event coreEvent = retrieveEvent(event, details.getId(), rank);
        eventRepository.insert(coreEvent);
    }


}
