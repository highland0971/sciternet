package controllers;

import model.PaymentType;
import model.ServerType;
import model.UsageAudit;
import model.User;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;


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

    public Result chargePlanBrief() {
        return ok(chargePlan.render());
    }

    @Transactional(readOnly = true)
    public Result jsonGetUsageDetails(Integer month) {
        if(session("user_id")!=null && month >=1 && month <=12)
        {
            try
            {
                int year = LocalDate.now().getYear();
                LocalDate today = LocalDate.of(year,month,1);
                int startDate = (int)TimeUtil.toOrdinal(( LocalDate.of(year,month,1)));
                int endDate = (int)TimeUtil.toOrdinal(( LocalDate.of(year,month,today.lengthOfMonth())));
                System.out.println("Start Date "+startDate+" End Date "+endDate);
                EntityManager em = jpaApi.em();
                User currentUser = em.find(User.class, Long.valueOf(session("user_id")));
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery criteria = cb.createQuery(UsageAudit.class);
                Root<UsageAudit> i = criteria.from(UsageAudit.class);

                ParameterExpression<Integer> startingDate = cb.parameter(Integer.class);
                ParameterExpression<Integer> endingDate = cb.parameter(Integer.class);

                criteria.multiselect(
                        i.<Integer>get("audit_date"),
                        cb.sum(i.<Integer>get("usage_gb")),
                        cb.sum(i.<Double>get("usage_mb"))
                );

                criteria.where(cb.and(
                        cb.between(i.<Integer>get("audit_date"),startingDate,endingDate),
                        cb.equal(i.get("auditedUser"),currentUser)
                ));
                criteria.orderBy(cb.asc(i.get("audit_date")));
                criteria.groupBy(i.<Integer>get("audit_date"));

                TypedQuery<UsageAudit> query = em.createQuery(criteria);
                query.setParameter(startingDate,startDate);
                query.setParameter(endingDate,endDate);

                try {
                    List<UsageAudit> result = query.getResultList();
                    Map<Integer,Double> jsonTarget = new HashMap<>();
                    for (UsageAudit audit : result) {

                        int day = TimeUtil.fromOrdinal(audit.getAudit_date()).getDayOfMonth();
                        double usage = audit.getUsage_gb()*1024+audit.getUsage_mb();
                        jsonTarget.put(day,usage);
                        System.out.println(String.valueOf(day)+":"+String.valueOf(usage));

                    }
                    return ok(Json.toJson(jsonTarget));
                }
                catch (NoResultException ex)
                {
                    ex.printStackTrace();
                    return internalServerError("Failed to fetch usage for user:"+session("user_id"));
                }

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return internalServerError("Failed to fetch usage for user:"+session("user_id"));
            }
        }
        return status(402,"Unexpected access!");
    }

    @Transactional(readOnly = true)
    public Result usageBrief() {
        if(session("user_id")!=null)
        {
            try
            {
                LocalDate today = LocalDate.now();
                int month = today.getMonthValue();

                int year = today.getYear();
                int startDate = (int)TimeUtil.toOrdinal(( LocalDate.of(year,month,1)));
                int endDate = (int)TimeUtil.toOrdinal(( LocalDate.of(year,month,today.lengthOfMonth())));

                EntityManager em = jpaApi.em();
                User currentUser = em.find(User.class, Long.valueOf(session("user_id")));
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery criteria = cb.createQuery(UsageAudit.class);
                Root<UsageAudit> i = criteria.from(UsageAudit.class);

                ParameterExpression<Integer> startingDate = cb.parameter(Integer.class);
                ParameterExpression<Integer> endingDate = cb.parameter(Integer.class);

                criteria.multiselect(
                        i.<Integer>get("audit_date"),
                        cb.sum(i.<Integer>get("usage_gb")),
                        cb.sum(i.<Double>get("usage_mb"))
                );

                criteria.where(cb.and(
                        cb.between(i.<Integer>get("audit_date"),startingDate,endingDate),
                        cb.equal(i.get("auditedUser"),currentUser)
                ));
                criteria.orderBy(cb.asc(i.get("audit_date")));
                criteria.groupBy(i.<Integer>get("audit_date"));

                TypedQuery<UsageAudit> query = em.createQuery(criteria);
                query.setParameter(startingDate,startDate);
                query.setParameter(endingDate,endDate);

                try {
                    List<UsageAudit> result = query.getResultList();
                    List<Integer> days = new ArrayList<Integer>();
                    List<Double> usages = new ArrayList<Double>();
                    for (UsageAudit audit : result) {

                        int day = TimeUtil.fromOrdinal(audit.getAudit_date()).getDayOfMonth();
                        //int day = audit.getAudit_date();
                        double usage = audit.getUsage_gb()*1024+audit.getUsage_mb();
                        days.add(day);
                        usages.add(usage);
                        System.out.println(String.valueOf(day)+":"+String.valueOf(usage));
                    }
                    List<Integer> months = new ArrayList();
                    for(int c = 0 ; c < today.getMonthValue();c++)
                    {
                        months.add(c+1);
                    }
                    return ok(usageAccount.render(months));
                }
                catch (NoResultException ex)
                {
                    ex.printStackTrace();
                    return internalServerError("Failed to fetch usage for user:"+session("user_id"));
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return internalServerError("Failed to fetch usage for user:"+session("user_id"));
            }
        }
        return status(402,"Unexpected access!");
    }

    @Transactional(readOnly = true)
    public Result trialBrief() {
        if(session("user_id")!=null)
        {
            try {
                EntityManager em = jpaApi.em();
                //TODO add try ... catch
                User user = em.find(User.class, Long.valueOf(session("user_id")));
                String brief = "Email " + user.getEmail();
                brief += "Credit volumn " + user.getCredit_data_gb();
                brief += "Vaild through " + TimeUtil.fromOrdinal(user.getExpire_date());
                return ok(TrialBrief.render(brief));
            }catch (Exception ex)
            {
                ex.printStackTrace();
                System.out.println("Session id "+session("user_id"));
                return internalServerError();
            }
        }
        return status(402,"Unexpected access!");
    }

    @Transactional
    public Result login() {

        DynamicForm requestData = formFactory.form().bindFromRequest();

        Form<User> userLoginForm = formFactory.form(User.class);
        //User user = userLoginForm.bindFromRequest().get();

        EntityManager em = jpaApi.em();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery criteria = cb.createQuery(User.class);
        Root<User> i = criteria.from(User.class);

        ParameterExpression<String> emailPara = cb.parameter(String.class);
        ParameterExpression<String> pwdPara = cb.parameter(String.class);

        criteria.select(i).where(cb.and(
                cb.equal(i.get("email"),emailPara),
                cb.equal(i.get("password"),pwdPara)
        ));

        TypedQuery<User> query = em.createQuery(criteria);
        query.setParameter(emailPara,requestData.get("email"));
        query.setParameter(pwdPara,requestData.get("password"));

        try
        {
            User qualifiedUser = query.getSingleResult();
            session("user_id",Long.toString(qualifiedUser.getUser_id()));
            session("user_email",qualifiedUser.getEmail());
            return usageBrief();
        }
        catch (NoResultException ex)
        {
            return ok("Failed to verify email:"+requestData.get("email")+" password:" + requestData.get("password"));
        }
    }

    public Result logout() {
        session().remove("user_id");
        return redirect(routes.HomeController.welcome());
    }

    public Result welcome() {
        Form<User> userLoginForm = formFactory.form(User.class);
        //return ok(framework.render(userLoginForm,false));
        return ok(welcome.render(userLoginForm, false));
    }

    public Result trialRegistration(){
        Form<User> userLoginForm = formFactory.form(User.class);
        return ok(TrialRegistration.render(userLoginForm,session("user_id")!=null,session("user_id")));
    }

    @Transactional
    public Result accountVerify(){
        /**
         * Verify account registration request according to email verification code.If passed,added to database.
         */
        Form<User> userLoginForm = formFactory.form(User.class);
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String code = requestData.get("code");
        if(code != null && session("verify_code") != null )
        {
            if(code.equals(session("verify_code")) )
            {
                EntityManager em = jpaApi.em();

                User newUser = new User();
                newUser.setEmail(session("session_email"));

                //TODO
                //Need to be salted
                newUser.setPassword(session("session_pwd"));
                newUser.setCredit_data_gb(1);

                newUser.setExpire_date(TimeUtil.toOrdinal(LocalDate.now())+31);
                newUser.setPayment_type(PaymentType.usage_duration);
                newUser.setReg_data(TimeUtil.toOrdinal(LocalDate.now()));
                newUser.setServer_type(ServerType.shared);

                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(session("session_email").getBytes());
                    md.update(session("session_pwd").getBytes());
                    String token = "";
                    for(byte b : md.digest())
                    {
                        token += String.valueOf(b);
                    }
                    newUser.setToken(token);
                }
                catch (NoSuchAlgorithmException ex)
                {
                    newUser.setToken(session("session_pwd"));
                }

                em.persist(newUser);
                session("user_id",Long.toString(newUser.getUser_id()));
                session("user_email",newUser.getEmail());

                session().remove("verify_code");
                session().remove("session_email");
                session().remove("session_pwd");

                return firmwareDownload();
            }
            else
            {
                String session_code = session("verify_code");
                session().remove("verify_code");
                return status(401,"Vefication Code Error. in session "+ session_code + " got "+code);
            }
        }
        else
        {
            String email = requestData.get("email");
            String password = requestData.get("password");
            Random rand = new Random();
            int randNum = rand.nextInt(10)*1000+rand.nextInt(10)*100+rand.nextInt(10)*10+rand.nextInt(10);
            session("verify_code",String.valueOf(randNum));
            session("session_email",email);
            session("session_pwd",password);

            String Message = "Your verifaction code is "+randNum;
            try {
                MailUtil.sendMail("15802221580@139.com", "15802221580@139.com", "jKr00t00", email, "Verification", Message);
                return ok(emailVerify.render(userLoginForm,session("user_id")!=null,session("user_id")));
            }
            catch (Exception ex){
                ex.printStackTrace();
                return internalServerError("What happened to "+email + password);
            }
        }
    }

    public Result firmwareDownload(){

        Map<String, Map<String,String> > firmware = new HashMap<>();

        String[] brand = {"Netgear","ASUS","TP-Link"};

        for (String ibrand:brand)
        {
            Map<String,String> brand_firm = new HashMap<>();
            brand_firm.put("XXX","123.firm");
            brand_firm.put("122X","124.firm");
            firmware.put(ibrand,brand_firm);
        }
        return ok(firmwareDownload.render(firmware));
    }
}
