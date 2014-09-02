/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.integration.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.runtime.process.NodeInstance;
import org.jboss.bpm.console.client.model.HumanTaskNodeRef;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef.RESULT;
import org.jboss.bpm.console.client.model.ProcessInstanceRef.STATE;
import org.jbpm.persistence.processinstance.ProcessInstanceComment;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.node.HumanTaskNode;

public class ProcessManagement  extends SessionInitializer implements org.jboss.bpm.console.server.integration.ProcessManagement {

    public ProcessManagement() {
        super();
    }
    
    public List<ProcessDefinitionRef> getProcessDefinitions() {
        List<Process> processes = CommandDelegate.getProcesses();
        List<ProcessDefinitionRef> result = new ArrayList<ProcessDefinitionRef>();
        for (Process process: processes) {
            result.add(Transform.processDefinition(process));
        }
        return result;
    }

    public ProcessDefinitionRef getProcessDefinition(String definitionId) {
        Process process = CommandDelegate.getProcess(definitionId);
        return Transform.processDefinition(process);
    }

    /**
     * method unsupported
     */
    public List<ProcessDefinitionRef> removeProcessDefinition(String definitionId) {
        CommandDelegate.removeProcess(definitionId); 
        return getProcessDefinitions();
    }
    
    
    public ProcessInstanceRef getProcessInstance(String instanceId) {
        ProcessInstanceLog processInstance = CommandDelegate.getProcessInstanceLog(instanceId);
        Collection<NodeInstance> activeNodes = CommandDelegate.getActiveNodeInstances(processInstance.getId());
        return Transform.processInstance(processInstance, activeNodes);
    }

    public List<ProcessInstanceRef> getProcessInstances(String definitionId) {
        List<ProcessInstanceLog> processInstances = CommandDelegate.getActiveProcessInstanceLogsByProcessId(definitionId);
        List<ProcessInstanceRef> result = new ArrayList<ProcessInstanceRef>();
        for (ProcessInstanceLog processInstance: processInstances) {
            
            Collection<NodeInstance> activeNodes = CommandDelegate.getActiveNodeInstances(processInstance.getId());
            result.add(Transform.processInstance(processInstance, activeNodes));
        }
        return result;
    }

    public ProcessInstanceRef newInstance(String definitionId) {
        ProcessInstanceLog processInstance = CommandDelegate.startProcess(definitionId, null);
        Collection<NodeInstance> activeNodes = CommandDelegate.getActiveNodeInstances(processInstance.getId());
        return Transform.processInstance(processInstance, activeNodes);
    }
    
    public ProcessInstanceRef newInstance(String definitionId, Map<String, Object> processVars) {
        ProcessInstanceLog processInstance = CommandDelegate.startProcess(definitionId, processVars);
        Collection<NodeInstance> activeNodes = CommandDelegate.getActiveNodeInstances(processInstance.getId());
        return Transform.processInstance(processInstance, activeNodes);
    }

    public void setProcessState(String instanceId, STATE nextState) {
        if (nextState == STATE.ENDED) {
            CommandDelegate.abortProcessInstance(instanceId);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    public Map<String, Object> getInstanceData(String instanceId) {
        return CommandDelegate.getProcessInstanceVariables(instanceId);
    }

    public void setInstanceData(String instanceId, Map<String, Object> data) {
        CommandDelegate.setProcessInstanceVariables(instanceId, data);
    }

    
    public void signalExecution(String executionId, String signal) {
        if (signal.indexOf("^") != -1) {
            String[] signalData = signal.split("\\^");
            CommandDelegate.signalExecution(executionId, signalData[0], signalData[1]);
        } else {
            CommandDelegate.signalExecution(executionId, signal, null);
        }
        
    }

    public void deleteInstance(String instanceId) {
        CommandDelegate.abortProcessInstance(instanceId);
    }

    //result means nothing
    public void endInstance(String instanceId, RESULT result) {
        CommandDelegate.abortProcessInstance(instanceId);
    }

    /**
     * Create a list of HumanTaskNode from de Process Definition id informed.
     * @param processId the id of the process definition
     * @return existing human task node for this process id, can be empty.
     */
    public List<HumanTaskNodeRef> getHumanTaskNodeListForProcess(String processId) {
        List<HumanTaskNodeRef> humanTaskNodeList = new ArrayList<HumanTaskNodeRef>();
        Process process = CommandDelegate.getProcess(processId);
        if(process != null) {
            WorkflowProcess workFlowProcess = (WorkflowProcess) process;
            getHumanTaskListData(workFlowProcess, humanTaskNodeList, processId);
        }

        return humanTaskNodeList;
    }

    private void getHumanTaskListData(NodeContainer container, List<HumanTaskNodeRef> result, String processId) {
        for (Node node: container.getNodes()) {
            if (node instanceof HumanTaskNode) {
                result.add(Transform.humanTaskNode((HumanTaskNode) node));
           } else if (node instanceof NodeContainer) {
                getHumanTaskListData((NodeContainer) node, result, processId);
            }
        }
    }

	public List<ProcessInstanceComment> getProcessInstanceComments(
			String instanceId) {
		return CommandDelegate.getProcessInstanceComments(instanceId);
	}

	public List<ProcessInstanceComment> getProcessInstanceComments(String instanceId, String group) {
		
		return CommandDelegate.getProcessInstanceComments(instanceId, group);
	}

}