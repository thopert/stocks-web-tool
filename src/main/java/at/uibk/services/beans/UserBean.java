package at.uibk.services.beans;

import at.uibk.model.mainEntities.User;

import javax.ejb.Stateless;
import javax.persistence.*;

@Stateless
public class UserBean {
    @PersistenceContext
    private EntityManager entityManager;

    public User find(long userId){
        return entityManager.find(User.class, userId);
    }

    public void persist(User user){
        this.entityManager.persist(user);
    }

    public void merge(User user) {
        entityManager.merge(user);
    }

    public void remove(User user){
        User toDelete = this.entityManager.find(User.class, user.getId());
        this.entityManager.remove(toDelete);
    }

    public User getUserByEmail(String email){
        TypedQuery<User> query = this.entityManager.createQuery(
                 "From " + User.class.getSimpleName() + " u " +
                         "Where u.email = :email", User.class);
        query.setParameter("email", email);

        try {
            User user = query.getSingleResult();
            return user;
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e){
            throw new IllegalStateException("Email: " + email + " exists multiple times!");
        }
    }

    public boolean isEmailInUse(String email){
        User user = getUserByEmail(email);
        return user != null;
    }
}
