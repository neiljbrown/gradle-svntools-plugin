package at.bxm.gradleplugins.svntools

import spock.lang.Specification

class SvnInfoTest extends Specification implements SvnTestSupport {

  def "execute at trunk"() {
    given: "an SVN workspace at trunk"
    createLocalRepo()
    def workspace = checkoutTrunk()

    when: "running the SvnInfo task"
    def project = projectWithPlugin()
    def task = project.tasks["svnInfo"] as SvnInfo
    task.sourcePath = workspace
    task.execute()

    then: "SVN data are available"
    def svnData = project.ext.svnData as SvnData
    svnData != null
    svnData.trunk == "trunk"
    svnData.branch == null
    svnData.tag == null
    svnData.revisionNumber == 1
  }

  def "execute at a branch"() {
    given: "an SVN workspace at a branch"
    createLocalRepo()
    def workspace = checkoutBranch()

    when: "running the SvnInfo task"
    def project = projectWithPlugin()
    def task = project.tasks["svnInfo"] as SvnInfo
    task.sourcePath = workspace
    task.execute()

    then: "SVN data are available"
    def svnData = project.ext.svnData as SvnData
    svnData != null
    svnData.trunk == null
    svnData.branch == "test-branch"
    svnData.tag == null
    svnData.revisionNumber == 1
  }

  def "execute at a tag"() {
    given: "an SVN workspace at a tag"
    createLocalRepo()
    def workspace = checkoutTag()

    when: "running the SvnInfo task"
    def project = projectWithPlugin()
    def task = project.tasks["svnInfo"] as SvnInfo
    task.sourcePath = workspace
    task.execute()

    then: "SVN data are available"
    def svnData = project.ext.svnData as SvnData
    svnData != null
    svnData.trunk == null
    svnData.branch == null
    svnData.tag == "test-tag"
    svnData.revisionNumber == 1
  }

  def "use custom property name"() {
    given: "an SVN workspace"
    createLocalRepo()
    def workspace = checkoutTrunk()

    when: "running the SvnInfo task"
    def project = projectWithPlugin()
    def task = project.tasks["svnInfo"] as SvnInfo
    task.sourcePath = workspace
    task.targetPropertyName = "myProp"
    task.execute()

    then: "SVN data are available with the right name"
    project.hasProperty("myProp")
    !project.hasProperty("svnData")
  }
}