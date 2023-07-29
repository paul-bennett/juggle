<!-- 
    Juggle -- an API search tool for Java
   
    Copyright 2020,2023 Paul Bennett
   
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
   
       http://www.apache.org/licenses/LICENSE-2.0
   
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
# Coverage Tests

This file contains invocations designed to increase code coverage of the test suite.  Many of the samples here
don't actually do anything useful, but instead the combination of command-line parameters have been carefully
selected in order to drive up the JaCoCo coverage metrics.


## Command-line Parsing

If we pass an invalid argument, we should get an error and the help text:

```shell
$ juggle --fiddle-de-dee
Unknown option: '--fiddle-de-dee'
Usage: juggle [-hVx] [--dry-run] [--show-query] [-c=none|all|auto] [-cp=path]
              [-f=auto|plain|colour|color] [-i=packageName] [-m=moduleName]
              [-p=modulePath] [-s=access|hierarchy|name|package|score|text]
              [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -c, --conversions=none|all|auto
                             Which conversions to apply
      -cp, --classpath, --class-path=path
                             JAR file or directory to include in search
      --dry-run              Dry run only
  -f, --format=auto|plain|colour|color
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -m, --module, --add-modules=moduleName
                             Modules to search
  -p, --module-path=modulePath
                             Where to look for modules
  -s, --sort=access|hierarchy|name|package|score|text
                             Sort criteria
      --show-query           Show query
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
```

Of course, we can explicitly ask for help:

```shell
$ juggle --help
Usage: juggle [-hVx] [--dry-run] [--show-query] [-c=none|all|auto] [-cp=path]
              [-f=auto|plain|colour|color] [-i=packageName] [-m=moduleName]
              [-p=modulePath] [-s=access|hierarchy|name|package|score|text]
              [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -c, --conversions=none|all|auto
                             Which conversions to apply
      -cp, --classpath, --class-path=path
                             JAR file or directory to include in search
      --dry-run              Dry run only
  -f, --format=auto|plain|colour|color
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -m, --module, --add-modules=moduleName
                             Modules to search
  -p, --module-path=modulePath
                             Where to look for modules
  -s, --sort=access|hierarchy|name|package|score|text
                             Sort criteria
      --show-query           Show query
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
```

We can ask for the version of the application, but when run from an unpacked
source tree it doesn't show anything useful:
```shell
$ juggle --version
juggle (unreleased version)
Java Runtime 8.0
$
```

## No parameters in the query

```shell
$ juggle NoSuchMethodException
public NoSuchMethodException.<init>()
public NoSuchMethodException.<init>(String)
$
```

## Missing module

```shell
$ juggle -m this.module.does.not.exist
*** Error: Module this.module.does.not.exist not found
$
```

## Unknown type

```shell
$ juggle 'boolean (ThisTypeDoesNotExist)'
*** Error: Couldn't find type: ThisTypeDoesNotExist
$
```

## Methods that don't throw

```shell
$ juggle 'String (? super java.io.InputStream) throws'
public String Object.toString()
public static String String.valueOf(Object)
public static String com.sun.corba.se.impl.util.RepositoryId.createSequenceRepID(Object)
public static String com.sun.imageio.plugins.common.ImageUtil.convertObjectToString(Object)
public static String com.sun.jndi.ldap.LdapName.escapeAttributeValue(Object)
public static String com.sun.org.apache.xalan.internal.lib.ExsltCommon.objectType(Object)
public static String com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.objectTypeF(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0001_UNSUPPORTED_MODEL_NODE_TYPE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0002_UNRECOGNIZED_SCOPE_TYPE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0004_UNEXPECTED_VISIBILITY_ATTR_VALUE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0005_UNEXPECTED_POLICY_ELEMENT_FOUND_IN_ASSERTION_PARAM(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0007_UNEXPECTED_MODEL_NODE_TYPE_FOUND(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0008_UNEXPECTED_CHILD_MODEL_TYPE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0010_UNEXPANDED_POLICY_REFERENCE_NODE_FOUND_REFERENCING(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0018_UNABLE_TO_ACCESS_POLICY_SOURCE_MODEL(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0019_SUBOPTIMAL_ALTERNATIVE_SELECTED(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0022_STORAGE_TYPE_NOT_SUPPORTED(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0023_UNEXPECTED_ERROR_WHILE_CLOSING_RESOURCE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0027_SERVICE_PROVIDER_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0028_SERVICE_PROVIDER_COULD_NOT_BE_INSTANTIATED(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0042_POLICY_REFERENCE_NODE_EXPECTED_INSTEAD_OF(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0050_OPERATION_NOT_SUPPORTED_FOR_THIS_BUT_POLICY_REFERENCE_NODE_TYPE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0051_OPERATION_NOT_SUPPORTED_FOR_THIS_BUT_ASSERTION_RELATED_NODE_TYPE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0052_NUMBER_OF_ALTERNATIVE_COMBINATIONS_CREATED(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0060_POLICY_ELEMENT_TYPE_UNKNOWN(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0063_ERROR_WHILE_CONSTRUCTING_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0065_INCONSISTENCY_IN_POLICY_SOURCE_MODEL(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0066_ILLEGAL_PROVIDER_CLASSNAME(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0069_EXCEPTION_WHILE_RETRIEVING_EFFECTIVE_POLICY_FOR_KEY(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0070_ERROR_REGISTERING_ASSERTION_CREATOR(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0076_NO_SERVICE_PROVIDERS_FOUND(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0077_ASSERTION_CREATOR_DOES_NOT_SUPPORT_ANY_URI(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0079_ERROR_WHILE_RFC_2396_UNESCAPING(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0086_FAILED_CREATE_READER(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0087_UNKNOWN_EVENT(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0088_FAILED_PARSE(Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0095_INVALID_BOOLEAN_VALUE(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.ACTION_NOT_SUPPORTED_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.ADDRESSING_NOT_ENABLED(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.ALREADY_HTTPS_SERVER(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.ALREADY_HTTP_SERVER(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.ANNOTATION_ONLY_ONCE(Object)
public static String com.sun.xml.internal.ws.resources.HandlerMessages.CANNOT_EXTEND_HANDLER_DIRECTLY(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.CAN_NOT_GENERATE_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.DISPATCH_CANNOT_FIND_METHOD(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.DUPLICATE_ABSTRACT_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.DispatchMessages.DUPLICATE_PORT(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.DUPLICATE_PORT_KNOWN_HEADER(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.DUPLICATE_PRIMARY_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_IMPLEMENTOR_FACTORY_NEW_INSTANCE_FAILED(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_IMPLEMENTOR_FACTORY_SERVANT_INIT_FAILED(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_IMPLEMENTOR_REGISTRY_CLASS_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_IMPLEMENTOR_REGISTRY_DUPLICATE_NAME(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_IMPLEMENTOR_REGISTRY_FILE_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_IMPLEMENTOR_REGISTRY_UNKNOWN_NAME(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_SERVLET_CAUGHT_THROWABLE(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_SERVLET_CAUGHT_THROWABLE_IN_INIT(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_SERVLET_CAUGHT_THROWABLE_WHILE_RECOVERING(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_SERVLET_INIT_CONFIG_FILE_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_SERVLET_INIT_CONFIG_PARAMETER_MISSING(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.ERROR_SERVLET_NO_IMPLEMENTOR_FOR_PORT(Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.ERROR_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.EXCEPTION_INCORRECT_TYPE(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.EXCEPTION_NOTFOUND(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.EXCEPTION_TRANSFORMATION_FAILED(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_EPR(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.FAILED_TO_READ_RESPONSE(Object)
public static String com.sun.xml.internal.ws.resources.HandlerMessages.HANDLER_CHAIN_CONTAINS_HANDLER_ONLY(Object)
public static String com.sun.xml.internal.ws.resources.HandlerMessages.HANDLER_NESTED_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.HandlerMessages.HANDLER_NOT_VALID_TYPE(Object)
public static String com.sun.xml.internal.ws.resources.HandlerMessages.HANDLER_PREDESTROY_IGNORE(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.HTTP_CLIENT_FAILED(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.HTTP_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.INVALID_ADDRESS(Object)
public static String com.sun.xml.internal.ws.resources.DispatchMessages.INVALID_QUERY_LEADING_CHAR(Object)
public static String com.sun.xml.internal.ws.resources.DispatchMessages.INVALID_QUERY_STRING(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.INVALID_SERVICE_NAME_NULL(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.INVALID_SERVICE_NO_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.DispatchMessages.INVALID_URI(Object)
public static String com.sun.xml.internal.ws.resources.DispatchMessages.INVALID_URI_RESOLUTION(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.INVALID_WSAW_ANONYMOUS(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.INVALID_WSDL_URL(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.LISTENER_PARSING_FAILED(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.LOCAL_CLIENT_FAILED(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0001_DEFAULT_CFG_FILE_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0003_DEFAULT_CFG_FILE_NOT_LOADED(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0004_NO_TUBELINES_SECTION_IN_DEFAULT_CFG_FILE(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0005_NO_DEFAULT_TUBELINE_IN_DEFAULT_CFG_FILE(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0006_APP_CFG_FILE_LOCATED(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0008_INVALID_URI_REFERENCE(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0009_CANNOT_FORM_VALID_URL(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0010_ERROR_READING_CFG_FILE_FROM_LOCATION(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0013_ERROR_INVOKING_SERVLET_CONTEXT_METHOD(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0014_UNABLE_TO_LOAD_CLASS(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0016_UNABLE_TO_INSTANTIATE_TUBE_FACTORY(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0017_UNABLE_TO_LOAD_TUBE_FACTORY_CLASS(Object)
public static String com.sun.xml.internal.ws.resources.TubelineassemblyMessages.MASM_0020_ERROR_CREATING_URI_FROM_GENERATED_STRING(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.MESSAGE_TOO_LONG(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.MISSING_HEADER_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.NESTED_DESERIALIZATION_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.NESTED_ENCODING_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.NESTED_MODELER_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.NESTED_SERIALIZATION_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.NON_ANONYMOUS_RESPONSE_NULL_HEADERS(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.NON_ANONYMOUS_RESPONSE_SENDING(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.NON_ANONYMOUS_UNKNOWN_PROTOCOL(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.NON_LOGICAL_HANDLER_SET(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_IMPLEMENT_PROVIDER(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_ZERO_PARAMETERS(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.NO_SUCH_CONTENT_ID(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.NO_SUNJAXWS_XML(Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NO_WSDL_NO_PORT(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.PROVIDER_NOT_PARAMETERIZED(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.PUBLISHER_INFO_APPLYING_TRANSFORMATION(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.PUBLISHER_INFO_GENERATING_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ADDRESSING_RESPONSES_NOSUCHMETHOD(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_CANNOT_GET_SERVICE_NAME_FROM_INTERFACE(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_CLASS_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ENDPOINT_INTERFACE_NO_WEBSERVICE(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_EXTERNAL_METADATA_GENERIC(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_EXTERNAL_METADATA_UNABLE_TO_READ(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_EXTERNAL_METADATA_WRONG_FORMAT(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_NO_OPERATIONS(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_NO_PACKAGE(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_NO_WEBSERVICE_ANNOTATION(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATIC(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATICFINAL(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_WEBMETHOD_MUST_BE_PUBLIC(Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_WRAPPER_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_CLASS_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_UNEXPECTED_CONTENT(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_NOSERVICE_IN_WSDLMODEL(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_XML_READER(Object)
public static String com.sun.xml.internal.ws.resources.SenderMessages.SENDER_NESTED_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.SenderMessages.SENDER_REQUEST_ILLEGAL_VALUE_FOR_CONTENT_NEGOTIATION(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.SERVER_RT_ERR(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_ERROR_NO_IMPLEMENTOR_FOR_ENDPOINT(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_FAULTSTRING_INTERNAL_SERVER_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_FAULTSTRING_PORT_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_HTML_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_NO_ADDRESS_AVAILABLE(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_TRACE_GOT_REQUEST_FOR_ENDPOINT(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_TRACE_INVOKING_IMPLEMENTOR(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_WARNING_DUPLICATE_ENDPOINT_URL_PATTERN(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.SERVLET_WARNING_IGNORING_IMPLICIT_URL_PATTERN(Object)
public static String com.sun.xml.internal.ws.resources.SoapMessages.SOAP_FACTORY_CREATE_ERR(Object)
public static String com.sun.xml.internal.ws.resources.SoapMessages.SOAP_FAULT_CREATE_ERR(Object)
public static String com.sun.xml.internal.ws.resources.SoapMessages.SOAP_MSG_CREATE_ERR(Object)
public static String com.sun.xml.internal.ws.resources.SoapMessages.SOAP_MSG_FACTORY_CREATE_ERR(Object)
public static String com.sun.xml.internal.ws.resources.SoapMessages.SOAP_PROTOCOL_INVALID_FAULT_CODE(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.SOURCEREADER_INVALID_SOURCE(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.STATEFUL_COOKIE_HEADER_REQUIRED(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.STATEFUL_INVALID_WEBSERVICE_CONTEXT(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.STATEFUL_REQURES_ADDRESSING(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.STAXREADER_XMLSTREAMEXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.STREAMING_IO_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.STREAMING_PARSE_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.TRACE_SERVLET_GOT_RESPONSE_FROM_IMPLEMENTOR(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.TRACE_SERVLET_HANDING_REQUEST_OVER_TO_IMPLEMENTOR(Object)
public static String com.sun.xml.internal.ws.resources.WsservletMessages.TRACE_SERVLET_REQUEST_FOR_PORT_NAMED(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNDEFINED_BINDING(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNDEFINED_PORT_TYPE(Object)
public static String com.sun.xml.internal.ws.resources.HttpserverMessages.UNEXPECTED_HTTP_METHOD(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.UNSUPPORTED_CHARSET(Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_HANDLER_CLASS_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_HANDLER_ENDPOINT_INTERFACE_NO_WEBSERVICE(Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_HANDLER_NO_WEBSERVICE_ANNOTATION(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.WOODSTOX_CANT_LOAD(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.WRONG_FIELD_TYPE(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.WRONG_NO_PARAMETERS(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.WRONG_PARAMETER_TYPE(Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.WRONG_TNS_FOR_PORT(Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.WSDL_BOUND_OPERATION_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.WSDL_CONTAINS_NO_SERVICE(Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_IMPORT_SHOULD_BE_WSDL(Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.WSDL_NOT_FOUND(Object)
public static String com.sun.xml.internal.ws.resources.ManagementMessages.WSM_1001_FAILED_ASSERTION(Object)
public static String com.sun.xml.internal.ws.resources.ManagementMessages.WSM_1002_EXPECTED_MANAGEMENT_ASSERTION(Object)
public static String com.sun.xml.internal.ws.resources.ManagementMessages.WSM_1003_MANAGEMENT_ASSERTION_MISSING_ID(Object)
public static String com.sun.xml.internal.ws.resources.ManagementMessages.WSM_1008_EXPECTED_INTEGER_DISPOSE_DELAY_VALUE(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1014_CAN_NOT_FIND_POLICY(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1016_POLICY_ID_NULL_OR_DUPLICATE(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1018_FAILED_TO_MARSHALL_POLICY(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1020_DUPLICATE_ID(Object)
public static String com.sun.xml.internal.ws.resources.PolicyMessages.WSP_1021_FAULT_NOT_BOUND(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_ILLEGAL_STATE_ENCOUNTERED(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_IO_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_NESTED_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_PARSE_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_CHARACTER_CONTENT(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLWRITER_IO_EXCEPTION(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLWRITER_NESTED_ERROR(Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLWRITER_NO_PREFIX_FOR_URI(Object)
public static String com.sun.xml.internal.ws.resources.XmlmessageMessages.XML_INVALID_CONTENT_TYPE(Object)
public static String com.sun.xml.internal.ws.resources.XmlmessageMessages.XML_ROOT_PART_INVALID_CONTENT_TYPE(Object)
public static String com.sun.xml.internal.ws.resources.EncodingMessages.XSD_UNKNOWN_PREFIX(Object)
public static String java.util.Objects.toString(Object)
public static String javax.naming.ldap.Rdn.escapeValue(Object)
public static String javax.swing.UIManager.getString(Object)
public static String sun.invoke.util.BytecodeDescriptor.unparse(Object)
public static String sun.util.logging.LoggingSupport.getLevelName(Object)
$
```

If it wasn't for the `throws` we'd expect the above query to also include 
`String java.net.URLConnection.guessContentTypeFromStream(java.io.InputStream)` in its results.


## Trying to find WildcardType

The biggest area that presently lacks test coverage is `TextOutput.decodeWildcardType()`.  This function
doesn't seem to be called at all, even when I explicitly search for a method that the JavaDoc suggests
returns a wildcard type:

```shell
$ juggle /asSubclass/
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
```

The `Class.asSubclass` method is declared to return `Class<? extends U>`. I suspect the wildcard is eliminated
at runtime due to type erasure, in which case it may be worth stripping this method from the source altogether.


## Empty -m

```shell
$ juggle -m '' /getUpperBound/
public java.lang.reflect.Type[] com.sun.beans.WildcardTypeImpl.getUpperBounds()
public abstract long com.sun.org.glassfish.external.statistics.BoundaryStatistic.getUpperBound()
public synchronized long com.sun.org.glassfish.external.statistics.impl.BoundaryStatisticImpl.getUpperBound()
public synchronized long com.sun.org.glassfish.external.statistics.impl.BoundedRangeStatisticImpl.getUpperBound()
public java.lang.reflect.Type[] com.sun.xml.internal.bind.v2.model.nav.WildcardTypeImpl.getUpperBounds()
public abstract java.lang.reflect.Type[] java.lang.reflect.WildcardType.getUpperBounds()
public abstract javax.lang.model.type.TypeMirror javax.lang.model.type.TypeVariable.getUpperBound()
public javax.management.ValueExp javax.management.BetweenQueryExp.getUpperBound()
public abstract int javax.swing.JTable.Resizable2.getUpperBoundAt(int)
public java.lang.reflect.Type[] sun.reflect.generics.reflectiveObjects.WildcardTypeImpl.getUpperBounds()
public sun.reflect.generics.tree.FieldTypeSignature[] sun.reflect.generics.tree.Wildcard.getUpperBounds()
$
```


## Multiple -t options

```shell
$ juggle throws java.io.NotActiveException, java.io.InvalidObjectException
public final synchronized void com.sun.corba.se.impl.io.IIOPInputStream.registerValidation(java.io.ObjectInputValidation,int) throws java.io.NotActiveException,java.io.InvalidObjectException
public void java.io.ObjectInputStream.registerValidation(java.io.ObjectInputValidation,int) throws java.io.NotActiveException,java.io.InvalidObjectException
$
```

## Explicit value of -x

By default, boolean arguments in picocli carry no value.  If you specify them on the command-line, the value `true`
is passed to the corresponding function.  

It feels wrong within the setter function to not use the value of the boolean parameter, even though we know it
will only ever take the value `true`.  That means JaCoCo will always present one path in an `if` statement as not
followed.

```shell
$ juggle '(String,ClassLoader,boolean)'
public static Class<T> com.sun.org.apache.xerces.internal.utils.ObjectFactory.findProviderClass(String,ClassLoader,boolean) throws ClassNotFoundException,com.sun.org.apache.xerces.internal.utils.ConfigurationError
public static Object com.sun.org.apache.xerces.internal.utils.ObjectFactory.newInstance(String,ClassLoader,boolean) throws com.sun.org.apache.xerces.internal.utils.ConfigurationError
public com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter.<init>(String,Object,boolean)
public javax.naming.Binding.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttribute.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttributes.<init>(String,Object,boolean)
public sun.font.Type1Font.<init>(String,Object,boolean) throws java.awt.FontFormatException
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.<init>(String,Object,Object)
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError.<init>(String,Object,Object)
public static void com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.runTimeError(String,Object,Object)
public static String com.sun.xml.internal.bind.marshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.bind.unmarshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNSUPPORTED_OPERATION(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableFAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ProviderApiMessages.localizableNOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableNOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableUNSUPPORTED_OPERATION(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.UtilMessages.localizableUTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.WsdlmodelMessages.localizableWSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableXMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
$
```

```shell
$ juggle -x '(String,ClassLoader,boolean)'
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
public static Class<T> com.sun.org.apache.xerces.internal.utils.ObjectFactory.findProviderClass(String,ClassLoader,boolean) throws ClassNotFoundException,com.sun.org.apache.xerces.internal.utils.ConfigurationError
public static Object com.sun.org.apache.xerces.internal.utils.ObjectFactory.newInstance(String,ClassLoader,boolean) throws com.sun.org.apache.xerces.internal.utils.ConfigurationError
public static org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.presentation.rmi.StubAdapter.request(Object,String,boolean)
public com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter.<init>(String,Object,boolean)
public javax.naming.Binding.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttribute.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttributes.<init>(String,Object,boolean)
public sun.font.Type1Font.<init>(String,Object,boolean) throws java.awt.FontFormatException
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.<init>(String,Object,Object)
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError.<init>(String,Object,Object)
public static void com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.runTimeError(String,Object,Object)
public static String com.sun.xml.internal.bind.marshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.bind.unmarshaller.Messages.format(String,Object,Object)
public static void sun.util.logging.LoggingSupport.log(Object,Object,String)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNSUPPORTED_OPERATION(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableFAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ProviderApiMessages.localizableNOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableNOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableUNSUPPORTED_OPERATION(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.UtilMessages.localizableUTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.WsdlmodelMessages.localizableWSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableXMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
$
```

But there's a workaround... add `negatable=true` to the `@Option` annotation, and 
suddenly the long option name can be prefixed with `no-` on the command-line.

```shell
$ juggle --permute '(String,ClassLoader,boolean)'
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
public static Class<T> com.sun.org.apache.xerces.internal.utils.ObjectFactory.findProviderClass(String,ClassLoader,boolean) throws ClassNotFoundException,com.sun.org.apache.xerces.internal.utils.ConfigurationError
public static Object com.sun.org.apache.xerces.internal.utils.ObjectFactory.newInstance(String,ClassLoader,boolean) throws com.sun.org.apache.xerces.internal.utils.ConfigurationError
public static org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.presentation.rmi.StubAdapter.request(Object,String,boolean)
public com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter.<init>(String,Object,boolean)
public javax.naming.Binding.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttribute.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttributes.<init>(String,Object,boolean)
public sun.font.Type1Font.<init>(String,Object,boolean) throws java.awt.FontFormatException
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.<init>(String,Object,Object)
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError.<init>(String,Object,Object)
public static void com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.runTimeError(String,Object,Object)
public static String com.sun.xml.internal.bind.marshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.bind.unmarshaller.Messages.format(String,Object,Object)
public static void sun.util.logging.LoggingSupport.log(Object,Object,String)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNSUPPORTED_OPERATION(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableFAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ProviderApiMessages.localizableNOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableNOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableUNSUPPORTED_OPERATION(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.UtilMessages.localizableUTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.WsdlmodelMessages.localizableWSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableXMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
$
```

```shell
$ juggle --no-permute '(String,ClassLoader,boolean)'
public static Class<T> com.sun.org.apache.xerces.internal.utils.ObjectFactory.findProviderClass(String,ClassLoader,boolean) throws ClassNotFoundException,com.sun.org.apache.xerces.internal.utils.ConfigurationError
public static Object com.sun.org.apache.xerces.internal.utils.ObjectFactory.newInstance(String,ClassLoader,boolean) throws com.sun.org.apache.xerces.internal.utils.ConfigurationError
public com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter.<init>(String,Object,boolean)
public javax.naming.Binding.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttribute.<init>(String,Object,boolean)
public javax.naming.directory.BasicAttributes.<init>(String,Object,boolean)
public sun.font.Type1Font.<init>(String,Object,boolean) throws java.awt.FontFormatException
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.<init>(String,Object,Object)
public com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError.<init>(String,Object,Object)
public static void com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.runTimeError(String,Object,Object)
public static String com.sun.xml.internal.bind.marshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.bind.unmarshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNSUPPORTED_OPERATION(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableFAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableINVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ProviderApiMessages.localizableNOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableNOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ModelerMessages.localizableRUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ServerMessages.localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableUNSUPPORTED_OPERATION(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.UtilMessages.localizableUTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.WsdlmodelMessages.localizableWSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.StreamingMessages.localizableXMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
$
```

## Missing dependency

The (contrived) App class from testApp uses the Lib class from testLib in its interface, but doesn't include these
dependent classes in the JAR (it's not an uberjar).  This means trying to load the App class fails.  

```shell
$ juggle -cp build/libs/testApp.jar 'com.angellane.juggle.testinput.app.App()'            
*** Warning: related class com.angellane.juggle.testinput.app.App: java.lang.NoClassDefFoundError: Lcom/angellane/juggle/testinput/lib/Lib;
*** Error: Couldn't find type: com.angellane.juggle.testinput.app.App
$
```


## Methods with no modifiers

Curiously this test fails here, but works in README.md.  See GitHub issue #39.
```shell
% juggle -cp build/libs/testLib.jar package com.angellane.juggle.testinput.lib.Lib
public static com.angellane.juggle.testinput.lib.Lib com.angellane.juggle.testinput.lib.Lib.libFactory()
com.angellane.juggle.testinput.lib.Lib.<init>()
%
```

## Dry-Run and Show-Query options

```shell
$ juggle --dry-run --show-query record
QUERY: TypeQuery{flavour=RECORD, annotationTypes=null, accessibility=PUBLIC, modifierMask=0, modifiers=0, declarationPattern=null, supertype=null, superInterfaces=null, subtype=null, isSealed=null, permittedSubtypes=null, recordComponents=null}
$
```
```shell
$ juggle --dry-run --show-query '()'
QUERY: MemberQuery{annotationTypes=null, accessibility=PUBLIC, modifierMask=0, modifiers=0, returnType=null, declarationPattern=null, params=[], exceptions=null}
$
```

## Classpath source

```shell
$ juggle -cp build/classes/java/main com.angellane.juggle.Juggler
public com.angellane.juggle.Juggler.<init>()
public com.angellane.juggle.Juggler com.angellane.juggle.Main.juggler
public com.angellane.juggle.Juggler com.angellane.juggle.source.Source.getJuggler()
$
```

```shell
$ juggle -cp this-path-does-not-exist             
*** Error: Couldn't locate this-path-does-not-exist
$
```

This test will only work on UNIX-like operating systems:
```shell
$ juggle -cp /dev/null
*** Error: Not a file or directory: `/dev/null'
$
```

And this one relies on `/etc/sudoers` being unreadable:
```shell
$ juggle -cp /etc/sudoers
*** Error: /etc/sudoers (Permission denied)
$
```
