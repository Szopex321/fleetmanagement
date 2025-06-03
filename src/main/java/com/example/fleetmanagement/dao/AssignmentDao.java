package com.example.fleetmanagement.dao;

import com.example.fleetmanagement.model.Assignment;
import com.example.fleetmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class AssignmentDao {

    public void save(Assignment assignment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(assignment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void update(Assignment assignment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(assignment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void delete(Assignment assignment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Assignment managedAssignment = session.get(Assignment.class, assignment.getId());
            if (managedAssignment != null) {
                session.remove(managedAssignment);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<Assignment> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Assignment> query = session.createQuery("SELECT a FROM Assignment a " + "LEFT JOIN FETCH a.vehicle " + "LEFT JOIN FETCH a.driver " + "ORDER BY a.startDate DESC, a.id DESC", Assignment.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /*
     * Sprawdza, czy istnieją nakładające się przypisania dla danego pojazdu w podanym okresie,
     * ignorując przypisanie o podanym ID (currentAssignmentId), co jest przydatne przy edycji istniejącego przypisania.
     * Dwa przedziały czasowe [sA, eA] (istniejące przypisanie 'a') i [sB, eB] (nowe/edytowane przypisanie 'param')
     * nakładają się, jeśli początek jednego jest przed lub w tym samym momencie co koniec drugiego, I ODWROTNIE.
     * Czyli: (sA <= eB) AND (sB <= eA).
     * Jeśli któryś koniec (eA lub eB) jest null, traktujemy go jako "do odległej przyszłości" (LocalDate.of(9999,12,31)).
     */
    public boolean hasOverlappingAssignmentForVehicle(Long vehicleId, LocalDate startDateNew, LocalDate endDateNew, Long currentAssignmentId) {
        if (vehicleId == null || startDateNew == null) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(a) FROM Assignment a WHERE a.vehicle.id = :vehicleId " +
                    "AND a.startDate <= :paramEndDateCompare " +
                    "AND COALESCE(a.endDate, :farFutureDate) >= :paramStartDateCompare ";

            // Jeśli sprawdzamy konflikt dla edytowanego przypisania, musimy je wykluczyć z porównania
            if (currentAssignmentId != null) {
                hql += "AND a.id != :currentAssignmentId";
            }

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("vehicleId", vehicleId);
            query.setParameter("paramStartDateCompare", startDateNew);
            query.setParameter("paramEndDateCompare", (endDateNew != null ? endDateNew : LocalDate.of(9999,12,31)));
            query.setParameter("farFutureDate", LocalDate.of(9999,12,31));

            if (currentAssignmentId != null) {
                query.setParameter("currentAssignmentId", currentAssignmentId);
            }

            Long count = query.uniqueResult();// Wykonanie zapytania, powinno zwrócić liczbę pasujących rekordów
            // Jeśli liczba jest większa niż 0, oznacza to, że znaleziono przynajmniej jedno nakładające się przypisanie
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /*
     * Sprawdza, czy istnieją nakładające się przypisania dla danego kierowcy w podanym okresie,
     * ignorując przypisanie o podanym ID (przydatne przy edycji).
     * Logika tej metody jest identyczna jak dla `hasOverlappingAssignmentForVehicle`,
     * tylko warunek WHERE odnosi się do `a.driver.id` zamiast `a.vehicle.id`.
     */
    public boolean hasOverlappingAssignmentForDriver(Long driverId, LocalDate startDateNew, LocalDate endDateNew, Long currentAssignmentId) {
        if (driverId == null || startDateNew == null) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(a) FROM Assignment a WHERE a.driver.id = :driverId " +
                    "AND a.startDate <= :paramEndDateCompare " +
                    "AND COALESCE(a.endDate, :farFutureDate) >= :paramStartDateCompare ";

            if (currentAssignmentId != null) {
                hql += "AND a.id != :currentAssignmentId";
            }

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("driverId", driverId);
            query.setParameter("paramStartDateCompare", startDateNew);
            query.setParameter("paramEndDateCompare", (endDateNew != null ? endDateNew : LocalDate.of(9999,12,31)));
            query.setParameter("farFutureDate", LocalDate.of(9999,12,31));

            if (currentAssignmentId != null) {
                query.setParameter("currentAssignmentId", currentAssignmentId);
            }

            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}