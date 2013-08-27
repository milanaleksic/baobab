package net.milanaleksic.baobab;

import static net.milanaleksic.baobab.util.ProxyFactoryForPostProcessingOfObservableMethods.MethodPostProcessor;

/**
 * User: Milan Aleksic
 * Date: 2/20/13
 * Time: 1:35 PM
 */
public class ModelAcceptanceTestModelDummyProxy extends ModelAcceptanceTestModel {

    private final MethodPostProcessor<ModelAcceptanceTest> model;

    public ModelAcceptanceTestModelDummyProxy(MethodPostProcessor<ModelAcceptanceTest> model) {
        this.model = model;
    }

    @Override
    public void setWithSideEffectValueOfData(Object data, int data1, long data2, double data3, float data4, Double data5, boolean data6) {
        super.setWithSideEffectValueOfData(data, data1, data2, data3, data4, data5, data6);
    }

}
