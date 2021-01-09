package cat.mrtxema.covid.datasource;

import java.util.Date;

public class CovidApiDataPoint {
    private String regionName;
    private String regionCode;
    private Date date;
    private String sex;
    private String ageGroup;
    private String nursingHome;
    private int confirmedCases;
    private int pcr;
    private int totalAdmissions;
    private int criticalAdmissions;
    private int totalAdmitted;
    private int criticalAdmitted;
    private int deaths;
    private int vaccinated;

    public String getRegionName() {
        return regionName;
    }

    public CovidApiDataPoint setRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public CovidApiDataPoint setRegionCode(String regionCode) {
        this.regionCode = regionCode;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public CovidApiDataPoint setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public CovidApiDataPoint setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public CovidApiDataPoint setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
        return this;
    }

    public String getNursingHome() {
        return nursingHome;
    }

    public CovidApiDataPoint setNursingHome(String nursingHome) {
        this.nursingHome = nursingHome;
        return this;
    }

    public int getConfirmedCases() {
        return confirmedCases;
    }

    public CovidApiDataPoint setConfirmedCases(int confirmedCases) {
        this.confirmedCases = confirmedCases;
        return this;
    }

    public int getPcr() {
        return pcr;
    }

    public CovidApiDataPoint setPcr(int pcr) {
        this.pcr = pcr;
        return this;
    }

    public int getTotalAdmissions() {
        return totalAdmissions;
    }

    public CovidApiDataPoint setTotalAdmissions(int totalAdmissions) {
        this.totalAdmissions = totalAdmissions;
        return this;
    }

    public int getCriticalAdmissions() {
        return criticalAdmissions;
    }

    public CovidApiDataPoint setCriticalAdmissions(int criticalAdmissions) {
        this.criticalAdmissions = criticalAdmissions;
        return this;
    }

    public int getTotalAdmitted() {
        return totalAdmitted;
    }

    public CovidApiDataPoint setTotalAdmitted(int totalAdmitted) {
        this.totalAdmitted = totalAdmitted;
        return this;
    }

    public int getCriticalAdmitted() {
        return criticalAdmitted;
    }

    public CovidApiDataPoint setCriticalAdmitted(int criticalAdmitted) {
        this.criticalAdmitted = criticalAdmitted;
        return this;
    }

    public int getDeaths() {
        return deaths;
    }

    public CovidApiDataPoint setDeaths(int deaths) {
        this.deaths = deaths;
        return this;
    }

    public int getVaccinated() {
        return vaccinated;
    }

    public CovidApiDataPoint setVaccinated(int vaccinated) {
        this.vaccinated = vaccinated;
        return this;
    }
}
