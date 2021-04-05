package cat.mrtxema.covid.reproduction;

import cat.mrtxema.covid.Configuration;
import cat.mrtxema.covid.io.IOUtil;
import cat.mrtxema.covid.io.ZipStreamReader;
import cat.mrtxema.covid.timeseries.IntegerDataPoint;
import cat.mrtxema.covid.timeseries.FloatDataPoint;
import cat.mrtxema.covid.timeseries.TimeSeriesHelper;
import com.github.rcaller.datatypes.DataFrame;
import com.github.rcaller.exception.ExecutionException;
import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCallerOptions;
import com.github.rcaller.rstuff.RCode;
import com.github.rcaller.rstuff.ROutputParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EpiEstim {
    private static final String EPIESTIM_SCRIPTS_ZIP = "epiestim.zip";

    public List<FloatDataPoint> estimateR(List<IntegerDataPoint> dataPoints) {
        ROutputParser parser = executeEpiEstimScript(transformInput(dataPoints));
        return transformOutput(parser, dataPoints.get(0).getDate());
    }

    private DataFrame transformInput(List<IntegerDataPoint> incidenceData) {
        return DataFrame.create(new Object[][] {
                incidenceData.stream().map(p -> p.getDate()).map(d -> (int) TimeUnit.DAYS.convert(d.getTime(), TimeUnit.MILLISECONDS)).collect(Collectors.toList()).toArray(),
                incidenceData.stream().map(p -> (double ) p.getValue()).collect(Collectors.toList()).toArray()
        }, new String[] {"dates", "I"});
    }

    private ROutputParser executeEpiEstimScript(DataFrame incidence) {
        RCode code = RCode.create();
        try {
            loadRScripts(code, "overall_infectivity.R", "discr_si.R", "utilities.R", "make_mcmc_control.R", "make_config.R", "estimate_r.R");
        } catch (IOException e) {
            throw new EpiEstimExecutionException("Error loading EpiEstim code", e);
        }
        code.addDataFrame("incidence", incidence);
        code.addRCode("incidence$dates <- as.numeric(incidence$dates)");
        code.addRCode(String.format("result <- estimate_R(incidence, method = \"parametric_si\", config = %s)", makeConfig()));
        RCaller caller = RCaller.create(code, RCallerOptions.create());
        try {
            caller.runAndReturnResult("result$R");
        } catch (ExecutionException e) {
            throw new EpiEstimExecutionException("Error executing EpiEstim code. Is R tool installed?", e);
        }
        return caller.getParser();
    }

    private List<FloatDataPoint> transformOutput(ROutputParser parser, Date initialDate) {
        List<Date> dates = parseDates(parser.getAsIntArray("t_end"), initialDate);
        float[] meanValues = parser.getAsFloatArray("MeanR");
        List<FloatDataPoint> result = new ArrayList<>();
        for (int i=0; i<meanValues.length; i++) {
            result.add(new FloatDataPoint().setDate(dates.get(i)).setValue(meanValues[i]));
        }
        return result;
    }

    private String makeConfig() {
        Configuration configuration = Configuration.getInstance();
        return String.format("make_config(list(mean_si = %f, std_si = %f))",
                configuration.getEpiestimMean(), configuration.getEpiestimStandardDeviation());
    }

    private void loadRScripts(RCode code, String... scriptFileNameArray) throws IOException {
        List<String> scriptFileNames = Arrays.asList(scriptFileNameArray);
        Map<String, String> scripts = new ZipStreamReader<>(zipEntry -> scriptFileNames.contains(zipEntry.getName()), IOUtil::readStream)
                .readAll(EpiEstim.class.getClassLoader().getResourceAsStream(EPIESTIM_SCRIPTS_ZIP));
        Arrays.stream(scriptFileNameArray).forEach(fileName -> code.addRCode(scripts.get(fileName)));
    }

    private List<Date> parseDates(int[] days, Date initialDate) {
        return Arrays.stream(days).mapToObj(day -> TimeSeriesHelper.addDays(initialDate, day - 1)).collect(Collectors.toList());
    }

    public static class EpiEstimExecutionException extends RuntimeException {

        public EpiEstimExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
