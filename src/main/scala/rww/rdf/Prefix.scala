package rww.rdf

import org.w3.banana.{PrefixBuilder, FOAFPrefix, RDFOps, RDF}

object ACLPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new ACLPrefix(ops)
}

class ACLPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("acl", "http://www.w3.org/ns/auth/acl#")(ops) {

  val Access = apply("Access")
  val Append = apply("Append")
  val Authorization = apply("Authorization")
  val Control = apply("Control")
  val Read = apply("Read")
  val Write = apply("Write")
  val accessControl = apply("accessControl")
  val accessTo = apply("accessTo")
  val accessToClass = apply("accessToClass")
  val agent = apply("agent")
  val agentClass = apply("agentClass")
  val defaultForNew = apply("defaultForNew")
  val delegates = apply("delegates")
  val mode = apply("mode")
  val owner = apply("owner")

}

object PIMWorkSpacePrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new PIMWorkSpacePrefix(ops)
}

class PIMWorkSpacePrefix[Rdf<:RDF](ops: RDFOps[Rdf])
   extends PrefixBuilder("pws","https://www.w3.org/ns/pim/space#")(ops) {

  val ControlledStorage = apply("ControlledStorage")
  val PersonalStorage = apply("PersonalStorage")
  val PublicStorage = apply("PublicStorage")
  val Storage = apply("Storage")
  val Workspace = apply("Workspace")
  val masterWorkspace = apply("masterWorkspace")
  val preferencesFile = apply("preferencesFile")
  val storage = apply("storage")
  val uriPrefix = apply("uriPrefix")
  val workspace = apply("workspace")

}