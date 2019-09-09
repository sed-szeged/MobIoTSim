package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;

public class HC_Population_Persons extends Population {

    public static final String UNTITLED_POPULATION = "unknown_people";

    public static final String PARAMETER_DISEASE_HASDIABETES_TYPE_1 = "HAS_DIABETES_TYPE1";
    public static final String PARAMETER_DISEASE_HAS_HYPERTONIA = "HAS_HYPERTONIA";
    public static final String PARAMETER_CARES_ABOUT_OWN_DIABETES = "CARES_ABOUT_OWN_DIABETES";
    public static final String PARAMETER_LIFESTYLE = "LIFESTYLE";
    public static final String PARAMETER_LIFESTYLE_ATHLETE = "ATHLETE";
    public static final String PARAMETER_LIFESTYLE_LAZYDEVELOPER = "LAZY_DEVELOPER";
    public static final String PARAMETER_LIFESTYLE_GOODWORKER = "GOOD_WORKER";
    public static final String PARAMETER_SCHEDULE = "SCHEDULE";
    public static final String PARAMETER_SCHEDULE_NOROUTINE = "USES_NO_DAILY_ROUTINE";
    public static final String PARAMETER_FEMALE_RATE = "FEMALE_RATE";
    //static final String PARAMETER_SCHEDULE_DAILYROUTINE = "USES_DAILY_ROUTINE";
    //static final String PARAMETER_SCHEDULE_WEEKLYROUTINE = "USES_WEEKLY_ROUTINE";
    public static final Parameter.ParamEnum.ParamEnumGroup GROUP_LIFESTYLE = new Parameter.ParamEnum.ParamEnumGroup(
            new String[]{PARAMETER_LIFESTYLE_ATHLETE, PARAMETER_LIFESTYLE_GOODWORKER,
                    PARAMETER_LIFESTYLE_LAZYDEVELOPER}
    );
    public static final Parameter.ParamEnum.ParamEnumGroup GROUP_SCHEDULE = new Parameter.ParamEnum.ParamEnumGroup(
            new String[]{PARAMETER_SCHEDULE_NOROUTINE}
    );

    private Map<String, Parameter.ParamBoolean> diseaseAttributes = new HashMap<>();
    private Parameter.ParamEnum lifeStyle;
    private Parameter.ParamEnum schedule;
    private Parameter.ParamInteger femaleRate;     //0-100%
    
    HC_Population_Persons() {
        super();    //Just for emphasizing that it will be called first
        requiredNumberOfSubjects.setValue(1);
        populationName.setString(UNTITLED_POPULATION);
        //HC_Population-specific attributes:
        diseaseAttributes.put(PARAMETER_DISEASE_HAS_HYPERTONIA,
                new Parameter.ParamBoolean(false));
        lifeStyle = new Parameter.ParamEnum(GROUP_LIFESTYLE, PARAMETER_LIFESTYLE_GOODWORKER);
        schedule = new Parameter.ParamEnum(GROUP_SCHEDULE, PARAMETER_SCHEDULE_NOROUTINE);
        femaleRate = new Parameter.ParamInteger(50, Parameter.ParamInteger.RATE);

        //populationName and requiredNumberOfSubject have been already added in super()
        for (Map.Entry<String, Parameter.ParamBoolean> entry : diseaseAttributes.entrySet()) {
            populationSettings.put(entry.getKey(), entry.getValue());
        }
        populationSettings.put(PARAMETER_LIFESTYLE, lifeStyle);
        populationSettings.put(PARAMETER_SCHEDULE, schedule);
        populationSettings.put(PARAMETER_FEMALE_RATE, femaleRate);
    }

    @Override
    protected List<Subject> generateSubjects(Simulation simulation) {
        Random rnd = new Random();
        ArrayList<Subject> newSubjects = new ArrayList<>();
        long newSubjectsCounter = Math.max(0,
                getRequiredNumberOfSubjects()- getActualNumberOfSubjects());

        for (long subject = 0, females = 0;
             subject < newSubjectsCounter;
             subject++) {

            HC_Subject_Person newSubject = (HC_Subject_Person) createSubject((HC_Simulation)simulation);
            newSubjects.add(newSubject);

            if (females < femaleRate.getValue() * newSubjectsCounter / 100) {
                newSubject.gender.setValue(HC_Subject_Person.PARAMETER_GENDER_FEMALE);
                females++;
                newSubject.forename.setString( femaleNameList[rnd.nextInt(femaleNameList.length)] );
            } else {
                newSubject.gender.setValue(HC_Subject_Person.PARAMETER_GENDER_MALE);
                newSubject.forename.setString( maleNameList[rnd.nextInt(maleNameList.length)] );
            }
            newSubject.surname.setString( surnameList[rnd.nextInt(surnameList.length)] );
            newSubject.getSubjectNameParameter().setString(
                    newSubject.forename.toString() + " " + newSubject.surname.toString()
            );
            //newSubject.hasDiabetesType1 = diseaseAttributes.get(PARAMETER_DISEASE_HASDIABETES_TYPE_1);
            //newSubject.caresAboutOwnDiabetes = diseaseAttributes.get(PARAMETER_CARES_ABOUT_OWN_DIABETES);
            newSubject.hasHypertonia = diseaseAttributes.get(PARAMETER_DISEASE_HAS_HYPERTONIA);
            newSubject.lifestyle.setValue(lifeStyle.toString());
            newSubject.schedule.setValue(schedule.toString());
        }
        return newSubjects;
    }

    @Override
    protected Subject createSubject(Simulation simulation) {
        return new HC_Subject_Person((HC_Simulation) simulation);
    }

    public static final String[] maleNameList = new String[] {
            "JAMES", "JOHN", "ROBERT", "MICHAEL", "WILLIAM", "DAVID", "RICHARD", "CHARLES", "JOSEPH", "THOMAS", "CHRISTOPHER", "DANIEL", "PAUL", "MARK", "DONALD", "GEORGE", "KENNETH", "STEVEN", "EDWARD", "BRIAN", "RONALD", "ANTHONY", "KEVIN", "JASON", "MATTHEW", "GARY", "TIMOTHY", "JOSE", "LARRY", "JEFFREY", "FRANK", "SCOTT", "ERIC", "STEPHEN", "ANDREW", "RAYMOND", "GREGORY", "JOSHUA", "JERRY", "DENNIS", "WALTER", "PATRICK", "PETER", "HAROLD", "DOUGLAS", "HENRY", "CARL", "ARTHUR", "RYAN", "ROGER", "JOE", "JUAN", "JACK", "ALBERT", "JONATHAN", "JUSTIN", "TERRY", "GERALD", "KEITH", "SAMUEL", "WILLIE", "RALPH", "LAWRENCE", "NICHOLAS", "ROY", "BENJAMIN", "BRUCE", "BRANDON", "ADAM", "HARRY", "FRED", "WAYNE", "BILLY", "STEVE", "LOUIS", "JEREMY", "AARON", "RANDY", "HOWARD", "EUGENE", "CARLOS", "RUSSELL", "BOBBY", "VICTOR", "MARTIN", "ERNEST", "PHILLIP", "TODD", "JESSE", "CRAIG", "ALAN", "SHAWN", "CLARENCE", "SEAN", "PHILIP", "CHRIS", "JOHNNY", "EARL", "JIMMY", "ANTONIO"
    };

    public static final String[] femaleNameList = new String[] {
            "MARY", "PATRICIA", "LINDA", "BARBARA", "ELIZABETH", "JENNIFER", "MARIA", "SUSAN", "MARGARET", "DOROTHY", "LISA", "NANCY", "KAREN", "BETTY", "HELEN", "SANDRA", "DONNA", "CAROL", "RUTH", "SHARON", "MICHELLE", "LAURA", "SARAH", "KIMBERLY", "DEBORAH", "JESSICA", "SHIRLEY", "CYNTHIA", "ANGELA", "MELISSA", "BRENDA", "AMY", "ANNA", "REBECCA", "VIRGINIA", "KATHLEEN", "PAMELA", "MARTHA", "DEBRA", "AMANDA", "STEPHANIE", "CAROLYN", "CHRISTINE", "MARIE", "JANET", "CATHERINE", "FRANCES", "ANN", "JOYCE", "DIANE", "ALICE", "JULIE", "HEATHER", "TERESA", "DORIS", "GLORIA", "EVELYN", "JEAN", "CHERYL", "MILDRED", "KATHERINE", "JOAN", "ASHLEY", "JUDITH", "ROSE", "JANICE", "KELLY", "NICOLE", "JUDY", "CHRISTINA", "KATHY", "THERESA", "BEVERLY", "DENISE", "TAMMY", "IRENE", "JANE", "LORI", "RACHEL", "MARILYN", "ANDREA", "KATHRYN", "LOUISE", "SARA", "ANNE", "JACQUELINE", "WANDA", "BONNIE", "JULIA", "RUBY", "LOIS", "TINA", "PHYLLIS", "NORMA", "PAULA", "DIANA", "ANNIE", "LILLIAN", "EMILY", "ROBIN"
    };

    public static final String[] surnameList = new String[] {
            "SMITH", "JOHNSON", "WILLIAMS", "BROWN", "JONES", "GARCIA", "MILLER", "DAVIS", "RODRIGUEZ", "MARTINEZ", "HERNANDEZ", "LOPEZ", "GONZALEZ", "WILSON", "ANDERSON", "THOMAS", "TAYLOR", "MOORE", "JACKSON", "MARTIN", "LEE", "PEREZ", "THOMPSON", "WHITE", "HARRIS", "SANCHEZ", "CLARK", "RAMIREZ", "LEWIS", "ROBINSON", "WALKER", "YOUNG", "ALLEN", "KING", "WRIGHT", "SCOTT", "TORRES", "NGUYEN", "HILL", "FLORES", "GREEN", "ADAMS", "NELSON", "BAKER", "HALL", "RIVERA", "CAMPBELL", "MITCHELL", "CARTER", "ROBERTS", "GOMEZ", "PHILLIPS", "EVANS", "TURNER", "DIAZ", "PARKER", "CRUZ", "EDWARDS", "COLLINS", "REYES", "STEWART", "MORRIS", "MORALES", "MURPHY", "COOK", "ROGERS", "GUTIERREZ", "ORTIZ", "MORGAN", "COOPER", "PETERSON", "BAILEY", "REED", "KELLY", "HOWARD", "RAMOS", "KIM", "COX", "WARD", "RICHARDSON", "WATSON", "BROOKS", "CHAVEZ", "WOOD", "JAMES", "BENNETT", "GRAY", "MENDOZA", "RUIZ", "HUGHES", "PRICE", "ALVAREZ", "CASTILLO", "SANDERS", "PATEL", "MYERS", "LONG", "ROSS", "FOSTER", "JIMENEZ", "POWELL", "JENKINS", "PERRY", "RUSSELL", "SULLIVAN", "BELL", "COLEMAN", "BUTLER", "HENDERSON", "BARNES", "GONZALES", "FISHER", "VASQUEZ", "SIMMONS", "ROMERO", "JORDAN", "PATTERSON", "ALEXANDER", "HAMILTON", "GRAHAM", "REYNOLDS", "GRIFFIN", "WALLACE", "MORENO", "WEST", "COLE", "HAYES", "BRYANT", "HERRERA", "GIBSON", "ELLIS", "TRAN", "MEDINA", "AGUILAR", "STEVENS", "MURRAY", "FORD", "CASTRO", "MARSHALL", "OWENS", "HARRISON", "FERNANDEZ", "MCDONALD", "WOODS", "WASHINGTON", "KENNEDY", "WELLS", "VARGAS", "HENRY", "CHEN", "FREEMAN", "WEBB", "TUCKER", "GUZMAN", "BURNS", "CRAWFORD", "OLSON", "SIMPSON", "PORTER", "HUNTER", "GORDON", "MENDEZ", "SILVA", "SHAW", "SNYDER", "MASON", "DIXON", "MUNOZ", "HUNT", "HICKS", "HOLMES", "PALMER", "WAGNER", "BLACK", "ROBERTSON", "BOYD", "ROSE", "STONE", "SALAZAR", "FOX", "WARREN", "MILLS", "MEYER", "RICE", "SCHMIDT", "GARZA", "DANIELS", "FERGUSON", "NICHOLS", "STEPHENS", "SOTO", "WEAVER", "RYAN", "GARDNER", "PAYNE", "GRANT", "DUNN", "KELLEY", "SPENCER", "HAWKINS"
    };
}
