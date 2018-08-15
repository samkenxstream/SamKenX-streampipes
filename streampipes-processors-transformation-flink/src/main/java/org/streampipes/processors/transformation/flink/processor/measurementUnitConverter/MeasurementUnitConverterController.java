/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.streampipes.processors.transformation.flink.processor.measurementUnitConverter;

import com.github.jqudt.Unit;
import org.streampipes.container.api.ResolvesContainerProvidedOptions;
import org.streampipes.model.graph.DataProcessorDescription;
import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.model.runtime.RuntimeOptions;
import org.streampipes.model.schema.EventProperty;
import org.streampipes.model.schema.EventPropertyPrimitive;
import org.streampipes.model.schema.PropertyScope;
import org.streampipes.model.staticproperty.RuntimeResolvableOneOfStaticProperty;
import org.streampipes.processors.transformation.flink.config.TransformationFlinkConfig;
import org.streampipes.sdk.builder.ProcessingElementBuilder;
import org.streampipes.sdk.builder.PropertyRequirementsBuilder;
import org.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.streampipes.sdk.extractor.ProcessingElementParameterExtractor;
import org.streampipes.sdk.helpers.*;
import org.streampipes.units.UnitProvider;
import org.streampipes.wrapper.flink.FlinkDataProcessorDeclarer;
import org.streampipes.wrapper.flink.FlinkDataProcessorRuntime;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MeasurementUnitConverterController extends
        FlinkDataProcessorDeclarer<MeasurementUnitConverterParameters> implements ResolvesContainerProvidedOptions {

  private static final String CONVERT_PROPERTY = "convert-property";
  private static final String OUTPUT_UNIT = "outputUnit";

  @Override
  public DataProcessorDescription declareModel() {


    return ProcessingElementBuilder.create("org.streampipes.processors.transformation.flink.measurement-unit-converter", "Measurement Unit " +
                    "Converter",
            "Converts a unit of measurement to another one")
            .iconUrl(TransformationFlinkConfig.getIconUrl("unit_conversion"))
            .requiredStream(StreamRequirementsBuilder
                    .create()
                    .requiredPropertyWithUnaryMapping(PropertyRequirementsBuilder
                            .create()
                            .measurementUnitPresence()
                            .build(), Labels.from
                            (CONVERT_PROPERTY, "Property", "The" +
                                    " property to convert"), PropertyScope.MEASUREMENT_PROPERTY)
                    .build())
            .requiredSingleValueSelectionFromContainer(Labels.from(OUTPUT_UNIT, "The output type unit of " +
                    "measurement", ""), "convert-property")
            .supportedProtocols(SupportedProtocols.kafka())
            .supportedFormats(SupportedFormats.jsonFormat())
            .outputStrategy(OutputStrategies.transform(TransformOperations
                    .dynamicMeasurementUnitTransformation(CONVERT_PROPERTY, OUTPUT_UNIT)))
            .build();
  }


  @Override
  public FlinkDataProcessorRuntime<MeasurementUnitConverterParameters> getRuntime(DataProcessorInvocation sepa, ProcessingElementParameterExtractor extractor) {

    String convertProperty = extractor.mappingPropertyValue(CONVERT_PROPERTY);
    String inputUnitId = extractor.measurementUnit(convertProperty, 0);
    String outputUnitId = sepa.getStaticProperties().stream().filter(sp -> sp
            .getInternalName().equals(OUTPUT_UNIT)).map(sp ->
            (RuntimeResolvableOneOfStaticProperty) sp).findFirst().get().getOptions().stream
            ().filter(o -> o.isSelected()).map(o -> o.getInternalName()).findFirst().get();
    extractor.selectedSingleValueFromRemote(OUTPUT_UNIT, String.class);

    Unit inputUnit = UnitProvider.INSTANCE.getUnit(inputUnitId);
    Unit outputUnit = UnitProvider.INSTANCE.getUnit(outputUnitId);

    MeasurementUnitConverterParameters staticParams = new MeasurementUnitConverterParameters(
            sepa,
            convertProperty,
            inputUnit,
            outputUnit
    );

    return new MeasurementUnitConverterProgram(staticParams);
  }

  @Override
  public List<RuntimeOptions> resolveOptions(String requestId, EventProperty linkedEventProperty) {
    if (linkedEventProperty instanceof EventPropertyPrimitive && ((EventPropertyPrimitive) linkedEventProperty)
            .getMeasurementUnit() != null) {
      Unit measurementUnit = UnitProvider.INSTANCE.getUnit(((EventPropertyPrimitive) linkedEventProperty)
              .getMeasurementUnit().toString());
      URI type = measurementUnit.getType();
      List<Unit> availableUnits = UnitProvider.INSTANCE.getUnitsByType(type);
      return availableUnits.stream().filter(unit -> !(unit.getResource().toString().equals(measurementUnit
              .getResource().toString()))).map
              (unit -> new
                      RuntimeOptions
                      (unit
                              .getLabel(), unit
                              .getResource()
                              .toString()))
              .collect(Collectors
                      .toList());
    } else {
      return new ArrayList<>();
    }
  }
}
