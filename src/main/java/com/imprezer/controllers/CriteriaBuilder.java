package com.imprezer.controllers;

import com.imprezer.model.EventCategory;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by robert on 20.06.16.
 */
public class CriteriaBuilder {

    private int filersApplied = 0;

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private List<Criteria> criterias = new LinkedList<>();

    public CriteriaBuilder withName(String name) {
        criterias.add(where("name").is(name));
        filersApplied++;
        return this;
    }

    public CriteriaBuilder onDateTime(String datetime) {
        try {
            String[] values = datetime.split(":");
            String[] dateVal = values[0].split("\\.");
            //TODO apply zone specific, currently searching for zone GMT+2!
            Date dateTime = new Date(
                    Integer.valueOf(dateVal[2]) - 1900,
                    Integer.valueOf(dateVal[1]) - 1,
                    Integer.valueOf(dateVal[0]),
                    Integer.valueOf(values[1]) % 24,
                    Integer.valueOf(values[2]),
                    Integer.valueOf(values[3])
            );
            criterias.add(where("beginDateTime").lte(dateTime));
            criterias.add(where("endDateTime").gte(dateTime));
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
        filersApplied++;
        return this;
    }

    public CriteriaBuilder onDate(String date) {
        filersApplied++;
        return null;
    }

    public CriteriaBuilder withAlcohol(Integer alcohol) {
        if (alcohol != 0) {
            criterias.add(where("alcohol").is(alcohol > 0));
            filersApplied++;
        }
        return this;
    }

    public CriteriaBuilder paid(Integer isPaid) {
        if (isPaid != 0) {
            criterias.add(where("paid").is(isPaid > 0));
            filersApplied++;
        }
        return this;
    }

    public CriteriaBuilder near2(Double lat, Double lng, Integer range) {
        //TODO verify range unit [km] (https://docs.mongodb.com/manual/reference/operator/query/maxDistance/)
        criterias.add(where("location").near(new Point(lat, lng)).maxDistance(range));
        filersApplied++;
        return this;
    }

    public Criteria build() {
        return new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()]));
    }

    public CriteriaBuilder withPlace(String place) {
        criterias.add(where("place").is(place));
        filersApplied++;
        return this;
    }

    public CriteriaBuilder includeOnlyCategories(List<Integer> inclCats) {
        // TODO refactor to do it more gently
        if (inclCats != null && !inclCats.isEmpty()) {
            Iterator<Integer> it = inclCats.iterator();
            Criteria c = null;
            while (it.hasNext()) {
                if (c == null) {
                    c = getIncludeCategoryCriteria(it.next());
                } else {
                    c.orOperator(getIncludeCategoryCriteria(it.next()));
                }
            }
            criterias.add(c);
            filersApplied++;
        }
        return this;

    }

    private Criteria getIncludeCategoryCriteria(Integer catVal) {
        return where("categories").is(EventCategory.ofValue(catVal).toString());
    }

    public CriteriaBuilder excludeCategories(List<Integer> exclCats) {
        if (exclCats != null && !exclCats.isEmpty()) {
            Iterator<Integer> it = exclCats.iterator();
            while (it.hasNext()) {
                criterias.add(where("categories").ne(EventCategory.ofValue(it.next())));
            }
            filersApplied++;
        }
        return this;
    }

    public int getAmountOfAppliedFilters() {
        return filersApplied;
    }

    public void andDetailsIdNotEqualTo(String id) {
        filersApplied++;
        criterias.add(where("detailsId").ne(id));
    }
}
