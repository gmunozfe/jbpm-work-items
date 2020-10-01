/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
 */

package org.jbpm.workitem.springboot.samples.events.listeners;

import java.util.concurrent.CountDownLatch;

import org.jbpm.services.api.ProcessService;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CountDownLatchEventListener extends DefaultProcessEventListener {

    private String expectedProcessId;
    private CountDownLatch latch;
    private String expectedNodeName;
    
    private String executingThread;
    
    @Autowired
    private ProcessService processService;

    private String variable;

    private Object result;

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public Object getResult() {
        return result;
    }
    
    public void configure(String processId, int threads) {
        this.expectedProcessId = processId;
        this.latch = new CountDownLatch(threads);
    }
    
    public void configureNode(String processId, String nodeName, int threads) {
        configure(processId, threads);
        this.expectedNodeName = nodeName;
    }
    
    public CountDownLatch getCountDown() {
        return this.latch;
    }
    
    public String getExecutingThread() {
        return this.executingThread;
    }
    
    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        if (this.latch != null && event.getProcessInstance().getProcessId().equals(expectedProcessId)) {
            this.executingThread = Thread.currentThread().getName();
            this.latch.countDown();
        }
    }
    
    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        if (variable != null) {
          result = processService.getProcessInstanceVariable(event.getProcessInstance().getId(), variable);
        }
    }
    
    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {
        if (this.latch != null && expectedNodeName !=null && 
            expectedNodeName.equals(processNodeLeftEvent.getNodeInstance().getNodeName())) {
            this.executingThread = Thread.currentThread().getName();
            this.latch.countDown();
        }
    }

}
