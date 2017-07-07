package org.streampipes.wrapper.flink.samples;


import org.streampipes.container.init.DeclarersSingleton;
import org.streampipes.container.standalone.init.StandaloneModelSubmitter;
import org.streampipes.wrapper.flink.samples.axoom.MaintenancePredictionController;
import org.streampipes.wrapper.flink.samples.batchstream.FirstBatchThenStreamController;
import org.streampipes.wrapper.flink.samples.breakdown.Prediction2BreakdownController;
import org.streampipes.wrapper.flink.samples.classification.number.NumberClassificationController;
import org.streampipes.wrapper.flink.samples.count.aggregate.CountAggregateController;
import org.streampipes.wrapper.flink.samples.delay.sensor.DelayController;
import org.streampipes.wrapper.flink.samples.delay.taxi.DelayTaxiController;
import org.streampipes.wrapper.flink.samples.elasticsearch.ElasticSearchController;
import org.streampipes.wrapper.flink.samples.enrich.timestamp.TimestampController;
import org.streampipes.wrapper.flink.samples.file.FileSinkController;
import org.streampipes.wrapper.flink.samples.hasher.FieldHasherController;
import org.streampipes.wrapper.flink.samples.healthindex.HealthIndexController;
import org.streampipes.wrapper.flink.samples.labelorder.LabelOrderController;
import org.streampipes.wrapper.flink.samples.peak.PeakDetectionController;
import org.streampipes.wrapper.flink.samples.rename.FieldRenamerController;
import org.streampipes.wrapper.flink.samples.spatial.gridenricher.SpatialGridEnrichmentController;
import org.streampipes.wrapper.flink.samples.statistics.StatisticsSummaryController;
import org.streampipes.wrapper.flink.samples.statistics.window.StatisticsSummaryControllerWindow;
import org.streampipes.wrapper.flink.samples.timetofailure.TimeToFailureController;

public class FlinkInit extends StandaloneModelSubmitter {

  public static void main(String[] args) {
    DeclarersSingleton.getInstance()
            //.add(new WordCountController())
            .add(new FirstBatchThenStreamController())
            .add(new DelayController())
            .add(new DelayTaxiController())
            .add(new LabelOrderController())
            .add(new ElasticSearchController())
            .add(new NumberClassificationController())
            .add(new TimestampController())
            .add(new FieldHasherController())
            .add(new FieldRenamerController())
            .add(new HealthIndexController())
            .add(new TimeToFailureController())
            .add(new CountAggregateController())
            .add(new FileSinkController())
            .add(new StatisticsSummaryController())
            .add(new Prediction2BreakdownController())
            .add(new SpatialGridEnrichmentController())
            .add(new MaintenancePredictionController())
            .add(new StatisticsSummaryControllerWindow())
            .add(new PeakDetectionController());


    DeclarersSingleton.getInstance().setPort(8094);
    new FlinkInit().init();
  }

}
