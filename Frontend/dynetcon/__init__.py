from py4j.protocol import Py4JError

from neuralogic import transformations
import dynet as dy


class ModelWeights():

    def __init__(self, lrnn_model):
        self.lrnn_model = lrnn_model
        self.dynet_model = dy.Model()

        self.weights = self.extract_weights(lrnn_model)

    def extract_weights(self, lrnn_model):
        weights = {}
        for weight in lrnn_model.weights:
            weights[weight.name] = self.dynet_model.add_parameters(self.size(weight.value.value))

        weights["zeroWeight"] = self.dynet_model.add_parameters(1)
        weights["unitWeight"] = self.dynet_model.add_parameters(1)

        return weights

    def size(self, value):
        if type(value) == float:
            return 1
        elif type(value) == list:
            if type(value[0]) == list:
                return (len(value[0]), len(value))
            return len(value)


class NetworkBuilder():

    def __init__(self, model_weights):
        self.model_weights = model_weights
        self.network_index = 0

    def build_network(self, lrnn_sample):
        lrnn_network = transformations.getRawNetwork(lrnn_sample)
        output_neuron_index = lrnn_sample.query.neuron.index

        neuron_indices = [0] * len(lrnn_network.allNeuronsTopologic)
        neuron_outputs = [0] * len(lrnn_network.allNeuronsTopologic)
        topo_neurons = lrnn_network.allNeuronsTopologic
        for i, neuron in enumerate(topo_neurons):
            neuron_indices[neuron.index] = i
            expr = self.build_neuron(neuron, neuron_outputs, neuron_indices)
            if output_neuron_index == neuron.index:
                return expr


    def build_neuron(self, neuron, neuron_outputs, neuron_indices):
        index = neuron_indices[neuron.index]
        name = neuron.id

        inputs = neuron.getInputs()
        input_count = inputs.size()

        if input_count == 0:  # factNeuron
            val = neuron.getRawState().getValue().value
            dim = self.model_weights.size(val)
            fact_value = dy.constant(dim, val)
            offset = neuron.offset
            if offset is not None:
                m_offset = self.model_weights.weights[offset.name]
                expr = fact_value + m_offset
            else:
                expr = fact_value
            neuron_outputs[index] = expr
            return expr

        input_exprs = []
        for input in inputs:
            input_exprs.append(neuron_outputs[neuron_indices[input.index]])
        # input_exprs = dy.concatenate(input_exprs)

        agg = neuron.getAggregation()
        if agg is not None:
            activation = self.getActivation(agg.getClass().getName())
        else:
            activation = None

        try:  # weighted neuron?
            offset = neuron.getOffset()
            weights = neuron.getWeights()

            m_offset = self.model_weights.weights[offset.name]
            m_weights = []
            for weight in weights:
                m_weights.append(self.model_weights.weights[weight.name])
            m_weights = dy.concatenate(m_weights)
            expr = activation(m_weights * dy.concatenate(input_exprs) + m_offset)

        except Py4JError as ex:  # unweighted neuron
            expr = dy.esum(input_exprs)

        neuron_outputs[index] = expr
        return expr

    def getActivation(self, activation_str):
        split = activation_str.split(".")
        if split[-1] == "Sigmoid":
            return dy.log_sigmoid
        elif split[-1] == "ReLu":
            return dy.rectify
