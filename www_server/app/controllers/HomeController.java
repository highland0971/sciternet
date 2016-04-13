package controllers;

import model.User_Info;
import play.data.Form;
import play.data.FormFactory;
import play.db.*;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */

public class HomeController extends Controller {

    @Inject FormFactory formFactory;
    @Inject Database db;

    JPAApi jpaApi;

    @Inject
    public HomeController(JPAApi api)
    {
        this.jpaApi = api;
    }

    @Transactional
    public Result login() {

        Form<User_Info> userLoginForm = formFactory.form(User_Info.class);
        User_Info user = userLoginForm.bindFromRequest().get();

        EntityManager em = jpaApi.em();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery criteria = cb.createQuery(User_Info.class);
        Root<User_Info> i = criteria.from(User_Info.class);

        ParameterExpression<String> emailPara = cb.parameter(String.class);
        ParameterExpression<String> pwdPara = cb.parameter(String.class);

        criteria.select(i).where(cb.and(
                cb.equal(i.get("email"),emailPara),
                cb.equal(i.get("password"),pwdPara)
        ));

        TypedQuery<User_Info> query = em.createQuery(criteria);
        query.setParameter(emailPara,user.getEmail());
        query.setParameter(pwdPara,user.getPassword());

        try
        {
            User_Info qualifiedUser = query.getSingleResult();
            //return ok("User " + qualifiedUser.getEmail()+ " payment type is "+qualifiedUser.getPayment_type());
            return ok(welcome.render(userLoginForm,true));
        }
        catch (NoResultException ex)
        {
            return ok("Failed to verify email:"+user.getEmail()+" password:" + user.getPassword());
        }
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result welcome() {
        Form<User_Info> userLoginForm = formFactory.form(User_Info.class);
        //return ok(framework.render(userLoginForm,false));
        return ok(welcome.render(userLoginForm, false));
    }
}
