package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses org.jschema.test.*

uses org.jschema.examples.Invoice

class InvoiceTest extends GosonTest {

  function testDocs() {
    var invoice = new Invoice() {
      :Id = 42,
      :Date = Date.Today,
      :BillingAddress = new Invoice.Address() {
        :Type = BUSINESS,
        :Line1 = "123 Main Street",
        :City = "Menlo Park",
        :State = "CA",
        :Country = "USA",
        :Zip = 12345
      },
      :Customers = {
        new Invoice.Customers() {
          :FirstName = "Ted",
          :LastName = "Smith",
          :Address = new Invoice.Address() {
            :Type = RESIDENTIAL,
            :Line1 = "1122 G Street",
            :City = "Stockton",
            :State = "CA",
            :Country = "USA",
            :Zip = 12345
          }
        }
      },
      :Items = {
        new Invoice.Items() { :Name = "Cornmeal", :Amount = 1, :TotalCost = 5.99 }
      }
    }
    print( invoice.write() )
    print( invoice.prettyPrint() )
    print( invoice.prettyPrint( 4 ) )

    invoice.descendents().whereTypeIs(org.jschema.examples.Invoice.Address)
                     .where( \ addr -> addr.State == "CA" )
                     .each( \ addr -> print( "Found CA Address : ${addr}" ) )

    invoice.find(org.jschema.examples.Invoice.Address)
           .where( \ addr -> addr.State == "CA" )
           .each( \ addr -> print( "Found CA Address : ${addr}" ) )

    invoice.find(org.jschema.examples.Invoice.Address)
           .each( \ addr -> print( "Address Parent : ${addr.parent()}" ) )

    var map = invoice.asJSON()
    print( "Raw ID: ${map.get("id")}" )
    map.put( "foo", "bar" )
  }

}