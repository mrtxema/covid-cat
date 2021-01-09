package cat.mrtxema.covid.reproduction;

import cat.mrtxema.covid.timeseries.IntegerDataPoint;
import cat.mrtxema.covid.timeseries.FloatDataPoint;
import com.github.rcaller.datatypes.DataFrame;
import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCallerOptions;
import com.github.rcaller.rstuff.RCode;
import com.github.rcaller.rstuff.ROutputParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EpiEstim {
    private static final String EPIESTIM_SCRIPTS_PATH = "epiestim/";

    public List<FloatDataPoint> estimateR(List<IntegerDataPoint> dataPoints) throws IOException {
        DataFrame incidence = DataFrame.create(new Object[][] {
                dataPoints.stream().map(p -> p.getDate()).map(d -> (int) TimeUnit.DAYS.convert(d.getTime(), TimeUnit.MILLISECONDS)).collect(Collectors.toList()).toArray(),
                dataPoints.stream().map(p -> (double ) p.getValue()).collect(Collectors.toList()).toArray()
        }, new String[] {"dates", "I"});

        RCode code = RCode.create();
        code.addRCode(loadScript("overall_infectivity.R"));
        code.addRCode(loadScript("discr_si.R"));
        code.addRCode(loadScript("utilities.R"));
        code.addRCode(loadScript("make_mcmc_control.R"));
        code.addRCode(loadScript("make_config.R"));
        code.addRCode(loadScript("estimate_r.R"));

        code.addDataFrame("incidence", incidence);
        code.addRCode("incidence$dates <- as.numeric(incidence$dates)");

        //https://harvardanalytics.shinyapps.io/covid19/
        code.addRCode("result <- estimate_R(incidence, method = \"parametric_si\", config = make_config(list(mean_si = 4.8, std_si = 2.3)))");
        RCaller caller = RCaller.create(code, RCallerOptions.create());
        caller.runAndReturnResult("result$R");

        ROutputParser parser = caller.getParser();
        List<Date> dates = parseDates(parser.getAsIntArray("t_end"), dataPoints.get(0).getDate());
        float[] meanValues = parser.getAsFloatArray("MeanR");
        List<FloatDataPoint> result = new ArrayList<>();
        for (int i=0; i<meanValues.length; i++) {
            result.add(new FloatDataPoint().setDate(dates.get(i)).setValue(meanValues[i]));
        }
        return result;
    }

    private String loadScript(String fileName) throws IOException {
        return readStream(EpiEstim.class.getClassLoader().getResourceAsStream(EPIESTIM_SCRIPTS_PATH + fileName));
    }

    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    private List<Date> parseDates(int[] days, Date initialDate) {
        List<Date> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int day : days) {
            calendar.setTime(initialDate);
            calendar.add(Calendar.DAY_OF_YEAR, day - 1);
            result.add(calendar.getTime());
        }
        return result;
    }
}
