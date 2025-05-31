package com.example.fleetmanagement.dao;

import com.example.fleetmanagement.model.Assignment;
import com.example.fleetmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

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
            } else {
                System.err.println("Nie znaleziono przypisania o ID: " + assignment.getId() + " do usunięcia.");
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
            Query<Assignment> query = session.createQuery(
                    "SELECT a FROM Assignment a " +
                            "LEFT JOIN FETCH a.vehicle " +  // LEFT JOIN FETCH gdyby vehicle_id lub driver_id były NULL
                            "LEFT JOIN FETCH a.driver " +
                            "ORDER BY a.startDate DESC, a.id DESC",
                    Assignment.class
            );
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}