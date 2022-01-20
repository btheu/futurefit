#! /bin/groovy
import PipelineTools
def tools = new PipelineTools()

node('slave-build') {

  tools.runPipeline({

    def rules = tools.defaultRules()

    properties([
      buildDiscarder(logRotator(numToKeepStr: '50'))
    ])

    def rule = tools.findRule(env.BRANCH_NAME,rules)
    def context = [:]

    context['COMPILE_MVN_IMAGE']  = 'maven:3.8-eclipse-temurin-17'

    if(rule.TYPE == 'RELEASE'){
      context['DEPLOY_MVN_TARGET'] = 'clean deploy'
      context['DEPLOY_MVN_PROFILE'] = '-P sonatype-oss-release -P estivate'
    }

    tools.checkoutBase(rule,context)

    tools.compileBase(rule,context)

    tools.deployToNexusBase(rule,context)

    tools.sonarBase(rule,context)
  })
}
