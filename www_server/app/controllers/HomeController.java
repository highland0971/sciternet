package controllers;

import model.UserLogin;
import play.data.Form;
import play.data.FormFactory;
import play.db.*;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */

public class HomeController extends Controller {

    @Inject FormFactory formFactory;
    @Inject Database db;

    public Result login() {

        Form<UserLogin> userLoginForm = formFactory.form(UserLogin.class);
        UserLogin user = userLoginForm.bindFromRequest().get();

        Connection connection = db.getConnection();
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ok("email:"+user.getEmail()+" password:" + user.getPassword());
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
        Form<UserLogin> userLoginForm = formFactory.form(UserLogin.class);
        return ok(framework.render(userLoginForm,false));}

}
