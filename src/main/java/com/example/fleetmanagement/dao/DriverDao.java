package com.example.fleetmanagement.dao;

import com.example.fleetmanagement.model.Driver;
import com.example.fleetmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class DriverDao {

    public void save(Driver driver) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(driver);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void update(Driver driver) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(driver);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void delete(Driver driver) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Driver managedDriver = session.get(Driver.class, driver.getId());
            if (managedDriver != null) {
                session.remove(managedDriver);
            } else {
                System.err.println("Nie znaleziono kierowcy o ID: " + driver.getId() + " do usunięcia.");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }


    public List<Driver> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Driver> query = session.createQuery("FROM Driver d ORDER BY d.lastName, d.firstName", Driver.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // Zwróć pustą listę w razie błędu
        }
    }
}