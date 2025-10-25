package com.crescer_aprender.escala.repository;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper to build dynamic Specifications for Escala.
 * Supported filter keys: id, mes, ano, data (or datas) as ISO date (yyyy-MM-dd), voluntario or voluntarioId
 */
public class EscalaSpecifications {

    public static Specification<Escala> byFilters(Map<String, String> filters) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (filters == null || filters.isEmpty()) {
                return builder.conjunction();
            }

            filters.forEach((key, value) -> {
                if (value == null) return;
                switch (key) {
                    case "id":
                        try {
                            predicates.add(builder.equal(root.get("id"), Long.valueOf(value)));
                        } catch (Exception ignored) {}
                        break;
                    case "mes":
                        try {
                            predicates.add(builder.equal(root.get("mes"), Integer.valueOf(value)));
                        } catch (Exception ignored) {}
                        break;
                    case "ano":
                        try {
                            predicates.add(builder.equal(root.get("ano"), Long.valueOf(value)));
                        } catch (Exception ignored) {}
                        break;
                    case "data":
                    case "datas":
                        try {
                            LocalDate date = LocalDate.parse(value);
                            // join element collection 'datas' and compare
                            jakarta.persistence.criteria.Join<?, ?> join = root.join("datas");
                            predicates.add(builder.equal(join, date));
                        } catch (Exception ignored) {
                        }
                        break;
                    case "voluntario":
                    case "voluntarioId":
                        try {
                            jakarta.persistence.criteria.Join<?, ?> joinV = root.join("voluntarios");
                            predicates.add(builder.equal(joinV.get("id"), Long.valueOf(value)));
                        } catch (Exception ignored) {
                        }
                        break;
                    default:
                        // fallback: try to compare attribute as string
                        try {
                            predicates.add(builder.equal(root.get(key).as(String.class), value));
                        } catch (Exception ignored) {
                        }
                }
            });

            query.distinct(true);
            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
