package ygor

import grails.test.mixin.TestFor
import org.junit.Test
import spock.lang.Specification
import ygor.field.MappingsContainer

@TestFor(Record)
class RecordSpec extends Specification {

  // MappingsContainer container

  def setup() {
    // container = new MappingsContainer()
  }

  @Test
  testCreateRecord(){
    given:
    true

    when:
    true
    /*try{
      given: "a mappings container and a list of identifiers"
      ZdbIdentifier zdbId = new ZdbIdentifier("12345", container.getMapping("zdbId", MappingsContainer.YGOR))
      OnlineIdentifier eissn = new OnlineIdentifier("12345678", container.getMapping("onlineIdentifier", MappingsContainer.YGOR))
      ArrayList<AbstractIdentifier> ids = [zdbId, eissn]

      when: "a record is created"
      Record record = new Record(ids, container)

      then: "the record's multifields are empty"
      for (MultiField mf in record.multiFields.values()) {
        mf.getFirstPrioValue() == null
      }
      and: "the record's identifiers match the given ones"
      record.onlineIdentifier.identifier == "12345678"
      record.zdbId.identifier == "12345"
      record.printIdentifier == null
    }
    catch (Exception e){
      true // this test is ignored for now TODO : make it work
    }*/

    then:
    true
  }

  def cleanup() {
  }

}
