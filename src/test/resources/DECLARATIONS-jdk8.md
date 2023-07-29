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
# Development of new declaration syntax

These tests don't necessarily make sense.  I came up with them
while developing the new declaration-style syntax.  As a result
they're a bit mix-and-match of both syntax styles.

## Just showing static members

All parts of the declaration are optional.  That means we can
use the declaration syntax to do curious things, such as filter
a search for only static members.

First, just looking for the return type:
```shell
$ juggle java.io.OutputStream
public java.io.OutputStream Runtime.getLocalizedOutputStream(java.io.OutputStream)
public abstract java.io.OutputStream Process.getOutputStream()
public java.io.OutputStream UNIXProcess.getOutputStream()
public abstract java.io.OutputStream com.sun.image.codec.jpeg.JPEGImageEncoder.getOutputStream()
public java.io.OutputStream com.sun.istack.internal.ByteArrayDataSource.getOutputStream()
public java.io.OutputStream com.sun.java.util.jar.pack.BandStructure.ByteBand.collectorStream()
public java.io.OutputStream com.sun.jndi.ldap.Connection.outStream
public abstract java.io.OutputStream com.sun.net.httpserver.HttpExchange.getResponseBody()
public synchronized java.io.OutputStream com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl.getOutputStream() throws java.io.IOException
public java.io.OutputStream com.sun.org.apache.xerces.internal.dom.DOMOutputImpl.getByteStream()
public abstract java.io.OutputStream com.sun.org.apache.xml.internal.serializer.Serializer.getOutputStream()
public abstract java.io.OutputStream com.sun.org.apache.xml.internal.serializer.WriterChain.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.EmptySerializer.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.SerializerTraceWriter.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.ToHTMLSAXHandler.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.ToStream.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.ToTextSAXHandler.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.ToUnknownStream.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.ToXMLSAXHandler.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.WriterToASCI.getOutputStream()
public java.io.OutputStream com.sun.org.apache.xml.internal.serializer.WriterToUTF8Buffered.getOutputStream()
public static java.io.OutputStream com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility.encode(java.io.OutputStream,String) throws com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException
public static java.io.OutputStream com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility.encode(java.io.OutputStream,String,String) throws com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException
public java.io.OutputStream com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimePartDataSource.getOutputStream() throws java.io.IOException
public java.io.OutputStream com.sun.xml.internal.messaging.saaj.soap.MessageFactoryImpl.listen(java.io.OutputStream)
public static java.io.OutputStream com.sun.xml.internal.messaging.saaj.util.FastInfosetReflection.FastInfosetResult_getOutputStream(javax.xml.transform.Result) throws Exception
public java.io.OutputStream com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetResult.getOutputStream()
public java.io.OutputStream com.sun.xml.internal.org.jvnet.staxex.Base64Data.Base64DataSource.getOutputStream()
public abstract java.io.OutputStream com.sun.xml.internal.org.jvnet.staxex.XMLStreamWriterEx.writeBinary(String) throws javax.xml.stream.XMLStreamException
public java.io.OutputStream com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferCreator.writeBinary(String) throws javax.xml.stream.XMLStreamException
public java.io.OutputStream com.sun.xml.internal.ws.encoding.DataHandlerDataSource.getOutputStream() throws java.io.IOException
public java.io.OutputStream com.sun.xml.internal.ws.encoding.MIMEPartStreamingDataHandler.StreamingDataSource.getOutputStream() throws java.io.IOException
public java.io.OutputStream com.sun.xml.internal.ws.encoding.MtomCodec.MtomStreamWriterImpl.writeBinary(String) throws javax.xml.stream.XMLStreamException
public java.io.OutputStream com.sun.xml.internal.ws.encoding.xml.XMLMessage.XmlDataSource.getOutputStream()
public java.io.OutputStream com.sun.xml.internal.ws.message.JAXBAttachment.getOutputStream() throws java.io.IOException
public static java.io.OutputStream com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream(javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
public abstract java.io.OutputStream com.sun.xml.internal.ws.transport.http.WSHTTPConnection.getOutput() throws java.io.IOException
public java.io.OutputStream com.sun.xml.internal.ws.transport.http.server.PortableConnectionImpl.getOutput() throws java.io.IOException
public java.io.OutputStream com.sun.xml.internal.ws.transport.http.server.ServerConnectionImpl.getOutput() throws java.io.IOException
public java.io.OutputStream com.sun.xml.internal.ws.util.ByteArrayDataSource.getOutputStream()
public java.io.OutputStream.<init>()
public abstract java.io.OutputStream java.net.CacheRequest.getBody() throws java.io.IOException
public java.io.OutputStream java.net.Socket.getOutputStream() throws java.io.IOException
public java.io.OutputStream java.net.URLConnection.getOutputStream() throws java.io.IOException
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.AsynchronousByteChannel)
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.WritableByteChannel)
public static transient java.io.OutputStream java.nio.file.Files.newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
public transient java.io.OutputStream java.nio.file.spi.FileSystemProvider.newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
public synchronized java.io.OutputStream java.rmi.server.LogStream.getOutputStream()
public abstract java.io.OutputStream java.sql.Clob.setAsciiStream(long) throws java.sql.SQLException
public abstract java.io.OutputStream java.sql.Blob.setBinaryStream(long) throws java.sql.SQLException
public abstract java.io.OutputStream java.sql.SQLXML.setBinaryStream() throws java.sql.SQLException
public java.io.OutputStream java.util.Base64.Encoder.wrap(java.io.OutputStream)
public abstract java.io.OutputStream javax.activation.DataSource.getOutputStream() throws java.io.IOException
public java.io.OutputStream javax.activation.DataHandler.getOutputStream() throws java.io.IOException
public java.io.OutputStream javax.activation.DataHandlerDataSource.getOutputStream() throws java.io.IOException
public java.io.OutputStream javax.activation.FileDataSource.getOutputStream() throws java.io.IOException
public java.io.OutputStream javax.activation.URLDataSource.getOutputStream() throws java.io.IOException
public java.io.OutputStream javax.print.StreamPrintService.getOutputStream()
public java.io.OutputStream javax.sql.rowset.serial.SerialClob.setAsciiStream(long) throws javax.sql.rowset.serial.SerialException,java.sql.SQLException
public java.io.OutputStream javax.sql.rowset.serial.SerialBlob.setBinaryStream(long) throws javax.sql.rowset.serial.SerialException,java.sql.SQLException
public abstract java.io.OutputStream javax.tools.FileObject.openOutputStream() throws java.io.IOException
public java.io.OutputStream javax.tools.ForwardingFileObject<F>.openOutputStream() throws java.io.IOException
public java.io.OutputStream javax.tools.SimpleJavaFileObject.openOutputStream() throws java.io.IOException
public java.io.OutputStream javax.xml.transform.stream.StreamResult.getOutputStream()
public abstract java.io.OutputStream javax.xml.ws.spi.http.HttpExchange.getResponseBody() throws java.io.IOException
public abstract java.io.OutputStream org.w3c.dom.ls.LSOutput.getByteStream()
public synchronized java.io.OutputStream sun.awt.image.codec.JPEGImageEncoderImpl.getOutputStream()
public abstract java.io.OutputStream sun.net.ftp.FtpClient.putFileStream(String,boolean) throws sun.net.ftp.FtpProtocolException,java.io.IOException
public java.io.OutputStream sun.net.ftp.FtpClient.putFileStream(String) throws sun.net.ftp.FtpProtocolException,java.io.IOException
public java.io.OutputStream sun.net.ftp.impl.FtpClient.putFileStream(String,boolean) throws sun.net.ftp.FtpProtocolException,java.io.IOException
public java.io.OutputStream sun.net.httpserver.ExchangeImpl.getResponseBody()
public java.io.OutputStream sun.net.httpserver.HttpExchangeImpl.getResponseBody()
public java.io.OutputStream sun.net.httpserver.HttpsExchangeImpl.getResponseBody()
public java.io.OutputStream sun.net.httpserver.Request.outputStream()
public java.io.OutputStream sun.net.www.http.HttpClient.getOutputStream()
public java.io.OutputStream sun.net.www.protocol.ftp.FtpURLConnection.getOutputStream() throws java.io.IOException
public synchronized java.io.OutputStream sun.net.www.protocol.http.HttpURLConnection.getOutputStream() throws java.io.IOException
public synchronized java.io.OutputStream sun.net.www.protocol.https.HttpsURLConnectionImpl.getOutputStream() throws java.io.IOException
public synchronized java.io.OutputStream sun.net.www.protocol.mailto.MailToURLConnection.getOutputStream() throws java.io.IOException
public java.io.OutputStream sun.nio.ch.SocketAdaptor.getOutputStream() throws java.io.IOException
public abstract java.io.OutputStream sun.rmi.transport.Connection.getOutputStream() throws java.io.IOException
public java.io.OutputStream sun.rmi.transport.proxy.HttpReceiveSocket.getOutputStream() throws java.io.IOException
public java.io.OutputStream sun.rmi.transport.proxy.HttpSendSocket.getOutputStream() throws java.io.IOException
public java.io.OutputStream sun.rmi.transport.proxy.WrappedSocket.getOutputStream() throws java.io.IOException
public synchronized java.io.OutputStream sun.rmi.transport.proxy.HttpSendSocket.writeNotify() throws java.io.IOException
public java.io.OutputStream sun.rmi.transport.tcp.TCPConnection.getOutputStream() throws java.io.IOException
public static final java.io.PrintStream System.err
public static final java.io.PrintStream System.out
public com.sun.corba.se.impl.corba.AnyImpl.AnyOutputStream.<init>(com.sun.corba.se.spi.orb.ORB)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.corba.AnyImpl.create_output_stream()
public static com.sun.corba.se.impl.encoding.CDROutputStream com.sun.corba.se.impl.corba.TypeCodeImpl.newOutputStream(com.sun.corba.se.spi.orb.ORB)
public com.sun.corba.se.impl.encoding.CDROutputObject.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.pept.protocol.MessageMediator,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte)
public com.sun.corba.se.impl.encoding.CDROutputObject.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.pept.protocol.MessageMediator,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte,int)
public com.sun.corba.se.impl.encoding.CDROutputObject.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.protocol.CorbaMessageMediator,com.sun.corba.se.spi.ior.iiop.GIOPVersion,com.sun.corba.se.spi.transport.CorbaConnection,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte)
public com.sun.corba.se.impl.encoding.CDROutputStream.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion,byte,boolean,com.sun.corba.se.impl.encoding.BufferManagerWrite,byte)
public com.sun.corba.se.impl.encoding.CDROutputStream.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion,byte,boolean,com.sun.corba.se.impl.encoding.BufferManagerWrite,byte,boolean)
public com.sun.corba.se.impl.encoding.CDROutputStream_1_0.<init>()
public com.sun.corba.se.impl.encoding.CDROutputStream_1_1.<init>()
public com.sun.corba.se.impl.encoding.CDROutputStream_1_2.<init>()
public com.sun.corba.se.impl.encoding.EncapsOutputStream.<init>(com.sun.corba.se.spi.orb.ORB)
public com.sun.corba.se.impl.encoding.EncapsOutputStream.<init>(com.sun.corba.se.spi.orb.ORB,boolean)
public com.sun.corba.se.impl.encoding.EncapsOutputStream.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion)
public com.sun.corba.se.impl.encoding.EncapsOutputStream.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion,boolean)
public com.sun.corba.se.impl.encoding.IDLJavaSerializationOutputStream.<init>(byte)
public com.sun.corba.se.impl.encoding.TypeCodeOutputStream.<init>(com.sun.corba.se.spi.orb.ORB)
public com.sun.corba.se.impl.encoding.TypeCodeOutputStream.<init>(com.sun.corba.se.spi.orb.ORB,boolean)
public com.sun.corba.se.impl.encoding.TypeCodeOutputStream com.sun.corba.se.impl.encoding.TypeCodeOutputStream.createEncapsulation(org.omg.CORBA.ORB)
public com.sun.corba.se.impl.encoding.TypeCodeOutputStream com.sun.corba.se.impl.encoding.TypeCodeOutputStream.getTopLevelStream()
public static com.sun.corba.se.impl.encoding.CDROutputStreamBase com.sun.corba.se.impl.encoding.CDROutputStream.OutputStreamFactory.newOutputStream(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion,byte)
public static com.sun.corba.se.impl.encoding.TypeCodeOutputStream com.sun.corba.se.impl.encoding.TypeCodeOutputStream.wrapOutputStream(org.omg.CORBA_2_3.portable.OutputStream)
public com.sun.corba.se.impl.io.IIOPOutputStream.<init>() throws java.io.IOException
public com.sun.corba.se.impl.io.OutputStreamHook.<init>() throws java.io.IOException
public static java.io.PrintStream com.sun.corba.se.impl.naming.cosnaming.NamingUtils.debugStream
public static java.io.PrintStream com.sun.corba.se.impl.naming.cosnaming.NamingUtils.errStream
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.orb.ORBSingleton.create_output_stream()
public synchronized org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.orb.ORBImpl.create_output_stream()
public com.sun.corba.se.impl.orbutil.HexOutputStream.<init>(java.io.StringWriter)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.presentation.rmi.ReflectiveTie._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.presentation.rmi.DynamicStubImpl.request(String,boolean)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.protocol.CorbaMessageMediatorImpl.createExceptionReply()
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.protocol.CorbaMessageMediatorImpl.createReply()
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.impl.protocol.CorbaClientDelegateImpl.request(org.omg.CORBA.Object,String,boolean)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.activation._ActivatorImplBase._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.activation._InitialNameServiceImplBase._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.activation._LocatorImplBase._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.activation._RepositoryImplBase._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.activation._ServerImplBase._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.activation._ServerManagerImplBase._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public com.sun.corba.se.spi.encoding.CorbaOutputObject.<init>(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion,byte,boolean,com.sun.corba.se.impl.encoding.BufferManagerWrite,byte,boolean)
public abstract org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.presentation.rmi.DynamicStub.request(String,boolean)
public static org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.presentation.rmi.StubAdapter.request(Object,String,boolean)
public abstract org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.protocol.CorbaMessageMediator.createExceptionReply()
public abstract org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.protocol.CorbaMessageMediator.createReply()
public com.sun.java.util.jar.pack.BandStructure.ByteCounter.<init>(java.io.OutputStream)
public com.sun.media.sound.RIFFWriter.<init>(java.io.File,String) throws java.io.IOException
public com.sun.media.sound.RIFFWriter.<init>(java.io.OutputStream,String) throws java.io.IOException
public com.sun.media.sound.RIFFWriter.<init>(String,String) throws java.io.IOException
public com.sun.media.sound.RIFFWriter com.sun.media.sound.RIFFWriter.writeChunk(String) throws java.io.IOException
public com.sun.media.sound.RIFFWriter com.sun.media.sound.RIFFWriter.writeList(String) throws java.io.IOException
public com.sun.org.apache.xml.internal.security.utils.DigesterOutputStream.<init>(com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm)
public com.sun.org.apache.xml.internal.security.utils.SignerOutputStream.<init>(com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm)
public com.sun.org.apache.xml.internal.security.utils.UnsyncBufferedOutputStream.<init>(java.io.OutputStream)
public com.sun.org.apache.xml.internal.security.utils.UnsyncByteArrayOutputStream.<init>()
public org.omg.CORBA.portable.OutputStream com.sun.org.omg.SendingContext._CodeBaseImplBase._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public com.sun.xml.internal.bind.v2.util.ByteArrayOutputStreamEx.<init>()
public com.sun.xml.internal.bind.v2.util.ByteArrayOutputStreamEx.<init>(int)
public com.sun.xml.internal.messaging.saaj.packaging.mime.internet.AsciiOutputStream.<init>(boolean,boolean)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream.<init>(java.io.OutputStream)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream.<init>(java.io.OutputStream,int)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.BEncoderStream.<init>(java.io.OutputStream)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream.<init>(java.io.OutputStream,boolean)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.QPEncoderStream.<init>(java.io.OutputStream)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.QPEncoderStream.<init>(java.io.OutputStream,int)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.UUEncoderStream.<init>(java.io.OutputStream)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.UUEncoderStream.<init>(java.io.OutputStream,String)
public com.sun.xml.internal.messaging.saaj.packaging.mime.util.UUEncoderStream.<init>(java.io.OutputStream,String,int)
public com.sun.xml.internal.messaging.saaj.util.ByteOutputStream.<init>()
public com.sun.xml.internal.messaging.saaj.util.ByteOutputStream.<init>(int)
public com.sun.xml.internal.org.jvnet.staxex.Base64EncoderStream.<init>(java.io.OutputStream)
public com.sun.xml.internal.org.jvnet.staxex.Base64EncoderStream.<init>(javax.xml.stream.XMLStreamWriter,java.io.OutputStream)
public com.sun.xml.internal.org.jvnet.staxex.ByteArrayOutputStreamEx.<init>()
public com.sun.xml.internal.org.jvnet.staxex.ByteArrayOutputStreamEx.<init>(int)
public com.sun.xml.internal.ws.util.ByteArrayBuffer.<init>()
public com.sun.xml.internal.ws.util.ByteArrayBuffer.<init>(byte[])
public com.sun.xml.internal.ws.util.ByteArrayBuffer.<init>(byte[],int)
public com.sun.xml.internal.ws.util.ByteArrayBuffer.<init>(int)
public com.sun.xml.internal.ws.util.NoCloseOutputStream.<init>(java.io.OutputStream)
public java.io.PrintStream java.io.PrintStream.append(char)
public java.io.PrintStream java.io.PrintStream.append(CharSequence)
public java.io.PrintStream java.io.PrintStream.append(CharSequence,int,int)
public transient java.io.PrintStream java.io.PrintStream.format(String,Object[])
public transient java.io.PrintStream java.io.PrintStream.format(java.util.Locale,String,Object[])
public java.io.BufferedOutputStream.<init>(java.io.OutputStream)
public java.io.BufferedOutputStream.<init>(java.io.OutputStream,int)
public java.io.ByteArrayOutputStream.<init>()
public java.io.ByteArrayOutputStream.<init>(int)
public java.io.DataOutputStream.<init>(java.io.OutputStream)
public java.io.FileOutputStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(java.io.File,boolean) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(java.io.FileDescriptor)
public java.io.FileOutputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(String,boolean) throws java.io.FileNotFoundException
public java.io.FilterOutputStream.<init>(java.io.OutputStream)
public java.io.ObjectOutputStream.<init>(java.io.OutputStream) throws java.io.IOException
public java.io.PipedOutputStream.<init>()
public java.io.PipedOutputStream.<init>(java.io.PipedInputStream) throws java.io.IOException
public java.io.PrintStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.PrintStream.<init>(java.io.File,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.io.PrintStream.<init>(java.io.OutputStream)
public java.io.PrintStream.<init>(java.io.OutputStream,boolean)
public java.io.PrintStream.<init>(java.io.OutputStream,boolean,String) throws java.io.UnsupportedEncodingException
public java.io.PrintStream.<init>(String) throws java.io.FileNotFoundException
public java.io.PrintStream.<init>(String,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public transient java.io.PrintStream java.io.PrintStream.printf(String,Object[])
public transient java.io.PrintStream java.io.PrintStream.printf(java.util.Locale,String,Object[])
public static synchronized java.io.PrintStream java.rmi.server.LogStream.getDefaultStream()
public static java.io.PrintStream java.rmi.server.RemoteServer.getLog()
public static java.rmi.server.LogStream java.rmi.server.LogStream.log(String)
public java.security.DigestOutputStream.<init>(java.io.OutputStream,java.security.MessageDigest)
public static java.io.PrintStream java.sql.DriverManager.getLogStream()
public java.util.jar.JarOutputStream.<init>(java.io.OutputStream) throws java.io.IOException
public java.util.jar.JarOutputStream.<init>(java.io.OutputStream,java.util.jar.Manifest) throws java.io.IOException
public java.util.zip.CheckedOutputStream.<init>(java.io.OutputStream,java.util.zip.Checksum)
public java.util.zip.DeflaterOutputStream.<init>(java.io.OutputStream)
public java.util.zip.DeflaterOutputStream.<init>(java.io.OutputStream,boolean)
public java.util.zip.DeflaterOutputStream.<init>(java.io.OutputStream,java.util.zip.Deflater)
public java.util.zip.DeflaterOutputStream.<init>(java.io.OutputStream,java.util.zip.Deflater,boolean)
public java.util.zip.DeflaterOutputStream.<init>(java.io.OutputStream,java.util.zip.Deflater,int)
public java.util.zip.DeflaterOutputStream.<init>(java.io.OutputStream,java.util.zip.Deflater,int,boolean)
public java.util.zip.GZIPOutputStream.<init>(java.io.OutputStream) throws java.io.IOException
public java.util.zip.GZIPOutputStream.<init>(java.io.OutputStream,boolean) throws java.io.IOException
public java.util.zip.GZIPOutputStream.<init>(java.io.OutputStream,int) throws java.io.IOException
public java.util.zip.GZIPOutputStream.<init>(java.io.OutputStream,int,boolean) throws java.io.IOException
public java.util.zip.InflaterOutputStream.<init>(java.io.OutputStream)
public java.util.zip.InflaterOutputStream.<init>(java.io.OutputStream,java.util.zip.Inflater)
public java.util.zip.InflaterOutputStream.<init>(java.io.OutputStream,java.util.zip.Inflater,int)
public java.util.zip.ZipOutputStream.<init>(java.io.OutputStream)
public java.util.zip.ZipOutputStream.<init>(java.io.OutputStream,java.nio.charset.Charset)
public org.omg.CORBA.portable.OutputStream javax.management.remote.rmi._RMIConnectionImpl_Tie._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler) throws org.omg.CORBA.SystemException
public org.omg.CORBA.portable.OutputStream javax.management.remote.rmi._RMIServerImpl_Tie._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler) throws org.omg.CORBA.SystemException
public static java.io.PrintStream javax.swing.DebugGraphics.logStream()
public javax.swing.text.rtf.AbstractFilter.<init>()
public javax.swing.text.rtf.RTFParser.<init>()
public javax.swing.text.rtf.RTFReader.<init>(javax.swing.text.StyledDocument)
public org.jcp.xml.dsig.internal.DigesterOutputStream.<init>(java.security.MessageDigest)
public org.jcp.xml.dsig.internal.DigesterOutputStream.<init>(java.security.MessageDigest,boolean)
public org.jcp.xml.dsig.internal.MacOutputStream.<init>(javax.crypto.Mac)
public org.jcp.xml.dsig.internal.SignerOutputStream.<init>(java.security.Signature)
public org.omg.CORBA.portable.OutputStream org.omg.CORBA.LocalObject._request(String,boolean)
public abstract org.omg.CORBA.portable.OutputStream org.omg.CORBA.Any.create_output_stream()
public abstract org.omg.CORBA.portable.OutputStream org.omg.CORBA.ORB.create_output_stream()
public abstract org.omg.CORBA.portable.OutputStream org.omg.CORBA.portable.InvokeHandler._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler) throws org.omg.CORBA.SystemException
public org.omg.CORBA.portable.OutputStream org.omg.CORBA.portable.ObjectImpl._request(String,boolean)
public abstract org.omg.CORBA.portable.OutputStream org.omg.CORBA.portable.ResponseHandler.createExceptionReply()
public abstract org.omg.CORBA.portable.OutputStream org.omg.CORBA.portable.ResponseHandler.createReply()
public org.omg.CORBA.portable.OutputStream.<init>()
public org.omg.CORBA.portable.OutputStream org.omg.CORBA.portable.Delegate.request(org.omg.CORBA.Object,String,boolean)
public org.omg.CORBA_2_3.portable.OutputStream.<init>()
public org.omg.CORBA.portable.OutputStream org.omg.CosNaming.BindingIteratorPOA._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream org.omg.CosNaming.NamingContextExtPOA._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream org.omg.CosNaming.NamingContextPOA._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream org.omg.PortableServer.ServantActivatorPOA._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream org.omg.PortableServer.ServantLocatorPOA._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
public org.omg.CORBA.portable.OutputStream org.omg.stub.javax.management.remote.rmi._RMIConnectionImpl_Tie._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler) throws org.omg.CORBA.SystemException
public org.omg.CORBA.portable.OutputStream org.omg.stub.javax.management.remote.rmi._RMIServerImpl_Tie._invoke(String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler) throws org.omg.CORBA.SystemException
public static com.sun.corba.se.impl.encoding.CDROutputObject sun.corba.OutputStreamFactory.newCDROutputObject(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.pept.protocol.MessageMediator,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte)
public static com.sun.corba.se.impl.encoding.CDROutputObject sun.corba.OutputStreamFactory.newCDROutputObject(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.pept.protocol.MessageMediator,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte,int)
public static com.sun.corba.se.impl.encoding.CDROutputObject sun.corba.OutputStreamFactory.newCDROutputObject(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.protocol.CorbaMessageMediator,com.sun.corba.se.spi.ior.iiop.GIOPVersion,com.sun.corba.se.spi.transport.CorbaConnection,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte)
public static com.sun.corba.se.impl.encoding.EncapsOutputStream sun.corba.OutputStreamFactory.newEncapsOutputStream(com.sun.corba.se.spi.orb.ORB)
public static com.sun.corba.se.impl.encoding.EncapsOutputStream sun.corba.OutputStreamFactory.newEncapsOutputStream(com.sun.corba.se.spi.orb.ORB,boolean)
public static com.sun.corba.se.impl.encoding.EncapsOutputStream sun.corba.OutputStreamFactory.newEncapsOutputStream(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion)
public static com.sun.corba.se.impl.encoding.TypeCodeOutputStream sun.corba.OutputStreamFactory.newTypeCodeOutputStream(com.sun.corba.se.spi.orb.ORB)
public static com.sun.corba.se.impl.encoding.TypeCodeOutputStream sun.corba.OutputStreamFactory.newTypeCodeOutputStream(com.sun.corba.se.spi.orb.ORB,boolean)
public static java.io.PrintStream sun.java2d.loops.GraphicsPrimitive.traceout
public java.io.ByteArrayOutputStream sun.misc.ProxyGenerator.MethodInfo.code
public java.io.PrintStream sun.net.NetworkServer.clientOutput
public java.io.PrintStream sun.net.NetworkClient.serverOutput
public sun.net.TelnetOutputStream.<init>(java.io.OutputStream,boolean)
public sun.net.httpserver.Request.WriteStream.<init>(sun.net.httpserver.ServerImpl,java.nio.channels.SocketChannel) throws java.io.IOException
public java.io.PrintStream sun.net.smtp.SmtpClient.startMessage() throws java.io.IOException
public sun.net.www.http.ChunkedOutputStream.<init>(java.io.PrintStream)
public sun.net.www.http.ChunkedOutputStream.<init>(java.io.PrintStream,int)
public sun.net.www.http.HttpCaptureOutputStream.<init>(java.io.OutputStream,sun.net.www.http.HttpCapture)
public sun.net.www.http.PosterOutputStream.<init>()
public sun.rmi.log.LogOutputStream.<init>(java.io.RandomAccessFile) throws java.io.IOException
public abstract java.io.PrintStream sun.rmi.runtime.Log.getPrintStream()
public java.io.PrintStream sun.rmi.runtime.Log.LogStreamLog.getPrintStream()
public synchronized java.io.PrintStream sun.rmi.runtime.Log.LoggerLog.getPrintStream()
public sun.rmi.server.MarshalOutputStream.<init>(java.io.OutputStream) throws java.io.IOException
public sun.rmi.server.MarshalOutputStream.<init>(java.io.OutputStream,int) throws java.io.IOException
public sun.rmi.transport.proxy.HttpOutputStream.<init>(java.io.OutputStream)
public sun.rmi.transport.proxy.HttpSendOutputStream.<init>(java.io.OutputStream,sun.rmi.transport.proxy.HttpSendSocket) throws java.io.IOException
public sun.security.krb5.internal.ccache.CCacheOutputStream.<init>(java.io.OutputStream)
public sun.security.krb5.internal.ktab.KeyTabOutputStream.<init>(java.io.OutputStream)
public sun.security.krb5.internal.util.KrbDataOutputStream.<init>(java.io.OutputStream)
public sun.security.util.DerOutputStream.<init>()
public sun.security.util.DerOutputStream.<init>(int)
$
```

Now let's do the same, but just show the `static` matches:
```shell
$ juggle static java.io.OutputStream
public static java.io.OutputStream com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility.encode(java.io.OutputStream,String) throws com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException
public static java.io.OutputStream com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility.encode(java.io.OutputStream,String,String) throws com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException
public static java.io.OutputStream com.sun.xml.internal.messaging.saaj.util.FastInfosetReflection.FastInfosetResult_getOutputStream(javax.xml.transform.Result) throws Exception
public static java.io.OutputStream com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream(javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.AsynchronousByteChannel)
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.WritableByteChannel)
public static transient java.io.OutputStream java.nio.file.Files.newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
public static final java.io.PrintStream System.err
public static final java.io.PrintStream System.out
public static com.sun.corba.se.impl.encoding.CDROutputStream com.sun.corba.se.impl.corba.TypeCodeImpl.newOutputStream(com.sun.corba.se.spi.orb.ORB)
public static com.sun.corba.se.impl.encoding.CDROutputStreamBase com.sun.corba.se.impl.encoding.CDROutputStream.OutputStreamFactory.newOutputStream(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion,byte)
public static com.sun.corba.se.impl.encoding.TypeCodeOutputStream com.sun.corba.se.impl.encoding.TypeCodeOutputStream.wrapOutputStream(org.omg.CORBA_2_3.portable.OutputStream)
public static java.io.PrintStream com.sun.corba.se.impl.naming.cosnaming.NamingUtils.debugStream
public static java.io.PrintStream com.sun.corba.se.impl.naming.cosnaming.NamingUtils.errStream
public static org.omg.CORBA.portable.OutputStream com.sun.corba.se.spi.presentation.rmi.StubAdapter.request(Object,String,boolean)
public static synchronized java.io.PrintStream java.rmi.server.LogStream.getDefaultStream()
public static java.io.PrintStream java.rmi.server.RemoteServer.getLog()
public static java.rmi.server.LogStream java.rmi.server.LogStream.log(String)
public static java.io.PrintStream java.sql.DriverManager.getLogStream()
public static java.io.PrintStream javax.swing.DebugGraphics.logStream()
public static com.sun.corba.se.impl.encoding.CDROutputObject sun.corba.OutputStreamFactory.newCDROutputObject(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.pept.protocol.MessageMediator,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte)
public static com.sun.corba.se.impl.encoding.CDROutputObject sun.corba.OutputStreamFactory.newCDROutputObject(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.pept.protocol.MessageMediator,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte,int)
public static com.sun.corba.se.impl.encoding.CDROutputObject sun.corba.OutputStreamFactory.newCDROutputObject(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.protocol.CorbaMessageMediator,com.sun.corba.se.spi.ior.iiop.GIOPVersion,com.sun.corba.se.spi.transport.CorbaConnection,com.sun.corba.se.impl.protocol.giopmsgheaders.Message,byte)
public static com.sun.corba.se.impl.encoding.EncapsOutputStream sun.corba.OutputStreamFactory.newEncapsOutputStream(com.sun.corba.se.spi.orb.ORB)
public static com.sun.corba.se.impl.encoding.EncapsOutputStream sun.corba.OutputStreamFactory.newEncapsOutputStream(com.sun.corba.se.spi.orb.ORB,boolean)
public static com.sun.corba.se.impl.encoding.EncapsOutputStream sun.corba.OutputStreamFactory.newEncapsOutputStream(com.sun.corba.se.spi.orb.ORB,com.sun.corba.se.spi.ior.iiop.GIOPVersion)
public static com.sun.corba.se.impl.encoding.TypeCodeOutputStream sun.corba.OutputStreamFactory.newTypeCodeOutputStream(com.sun.corba.se.spi.orb.ORB)
public static com.sun.corba.se.impl.encoding.TypeCodeOutputStream sun.corba.OutputStreamFactory.newTypeCodeOutputStream(com.sun.corba.se.spi.orb.ORB,boolean)
public static java.io.PrintStream sun.java2d.loops.GraphicsPrimitive.traceout
$
```

Or we could look at all the `static` methods returning `Package`
that have been marked as `@Deprecated`:

```shell
$ juggle @Deprecated static Package 
$
```

More example of modifiers:

```shell
$ juggle synchronized final
public final synchronized void Throwable.addSuppressed(Throwable)
public final synchronized Throwable[] Throwable.getSuppressed()
public final synchronized void Thread.join(long) throws InterruptedException
public final synchronized void Thread.join(long,int) throws InterruptedException
public final synchronized void Thread.setName(String)
public final synchronized void Thread.stop(Throwable)
public final synchronized Object com.sun.corba.se.impl.io.IIOPInputStream.readObjectDelegate() throws java.io.IOException
public final synchronized void com.sun.corba.se.impl.io.IIOPInputStream.registerValidation(java.io.ObjectInputValidation,int) throws java.io.NotActiveException,java.io.InvalidObjectException
public final synchronized void com.sun.corba.se.impl.io.IIOPInputStream.simpleSkipObject(String,com.sun.org.omg.SendingContext.CodeBase)
public final synchronized com.sun.corba.se.impl.util.RepositoryId com.sun.corba.se.impl.util.RepositoryIdCache.getId(String)
public final synchronized com.sun.corba.se.impl.util.RepositoryId com.sun.corba.se.impl.util.RepositoryIdPool.popId()
public final synchronized void com.sun.jmx.mbeanserver.ClassLoaderRepositorySupport.addClassLoader(javax.management.ObjectName,ClassLoader)
public final synchronized void com.sun.jmx.mbeanserver.ClassLoaderRepositorySupport.removeClassLoader(javax.management.ObjectName)
public final synchronized void com.sun.jmx.snmp.SnmpVarBindList.addVarBind(String[],String) throws com.sun.jmx.snmp.SnmpStatusException
public final synchronized Object com.sun.jmx.snmp.SnmpCounter64.clone()
public final synchronized Object com.sun.jmx.snmp.SnmpInt.clone()
public final synchronized Object com.sun.jmx.snmp.SnmpNull.clone()
public final synchronized void com.sun.jmx.snmp.SnmpVarBindList.concat(java.util.Vector<E>)
public final synchronized com.sun.jmx.snmp.SnmpValue com.sun.jmx.snmp.SnmpCounter64.duplicate()
public final synchronized com.sun.jmx.snmp.SnmpValue com.sun.jmx.snmp.SnmpInt.duplicate()
public final synchronized com.sun.jmx.snmp.SnmpValue com.sun.jmx.snmp.SnmpNull.duplicate()
public final synchronized com.sun.jmx.snmp.SnmpValue com.sun.jmx.snmp.SnmpOid.duplicate()
public final synchronized com.sun.jmx.snmp.SnmpValue com.sun.jmx.snmp.SnmpString.duplicate()
public final synchronized java.util.Date com.sun.jmx.snmp.Timestamp.getDate()
public final synchronized com.sun.jmx.snmp.SnmpValue com.sun.jmx.snmp.SnmpVarBind.getSnmpValue()
public final synchronized com.sun.jmx.snmp.SnmpTimeticks com.sun.jmx.snmp.Timestamp.getTimeTicks()
public final synchronized com.sun.jmx.snmp.SnmpVarBind com.sun.jmx.snmp.SnmpVarBindList.getVarBindAt(int)
public final synchronized int com.sun.jmx.snmp.SnmpPeer.getVarBindLimit()
public final synchronized boolean com.sun.jmx.snmp.SnmpVarBind.hasVarBindException()
public final synchronized String com.sun.jmx.snmp.SnmpPeer.ipAddressInUse()
public final synchronized void com.sun.jmx.snmp.SnmpVarBindList.replaceVarBind(com.sun.jmx.snmp.SnmpVarBind,int)
public final synchronized void com.sun.jmx.snmp.SnmpPeer.setDestPort(int)
public final synchronized void com.sun.jmx.snmp.SnmpPeer.setMaxSnmpPktSize(int)
public final synchronized void com.sun.jmx.snmp.SnmpPeer.setMaxTries(int)
public final synchronized void com.sun.jmx.snmp.SnmpPeer.setTimeout(int)
public final synchronized void com.sun.jmx.snmp.SnmpPeer.setVarBindLimit(int)
public final synchronized void com.sun.jmx.snmp.SnmpVarBindList.setVarBindList(java.util.Vector<E>)
public final synchronized void com.sun.jmx.snmp.SnmpVarBindList.setVarBindList(java.util.Vector<E>,boolean)
public final synchronized void com.sun.jmx.snmp.SnmpPeer.useAddressList(java.net.InetAddress[])
public final synchronized void com.sun.jmx.snmp.SnmpPeer.useIPAddress(String) throws java.net.UnknownHostException
public final synchronized void com.sun.jmx.snmp.SnmpPeer.useNextAddress()
public final synchronized long com.sun.jmx.snmp.daemon.SnmpInformRequest.getAbsMaxTimeToWait()
public final synchronized long com.sun.jmx.snmp.daemon.SnmpInformRequest.getAbsNextPollTime()
public final synchronized int com.sun.jmx.snmp.daemon.SnmpInformRequest.getErrorIndex()
public final synchronized int com.sun.jmx.snmp.daemon.SnmpInformRequest.getErrorStatus()
public final synchronized int com.sun.jmx.snmp.daemon.SnmpInformRequest.getNumTries()
public final synchronized int com.sun.jmx.snmp.daemon.SnmpInformRequest.getRequestId()
public final synchronized int com.sun.jmx.snmp.daemon.SnmpInformRequest.getRequestStatus()
public final synchronized com.sun.jmx.snmp.SnmpVarBindList com.sun.jmx.snmp.daemon.SnmpInformRequest.getResponseVarBindList()
public final synchronized boolean com.sun.jmx.snmp.daemon.SnmpInformRequest.inProgress()
public final synchronized boolean com.sun.jmx.snmp.daemon.SnmpInformRequest.isAborted()
public final synchronized boolean com.sun.jmx.snmp.daemon.SnmpInformRequest.isResultAvailable()
public final synchronized void com.sun.jmx.snmp.daemon.SnmpInformRequest.notifyClient()
public final synchronized void com.sun.jmx.snmp.daemon.SnmpAdaptorServer.setMaxTries(int)
public final synchronized void com.sun.jmx.snmp.daemon.SnmpAdaptorServer.setTimeout(int)
public final synchronized void com.sun.media.sound.AbstractMixer.close()
public final synchronized void com.sun.media.sound.AbstractMixer.open() throws javax.sound.sampled.LineUnavailableException
public final synchronized void com.sun.media.sound.AbstractMidiDevice.AbstractReceiver.send(javax.sound.midi.MidiMessage,long)
public static final synchronized com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory.getInstance() throws com.sun.org.apache.xerces.internal.impl.dv.DVFactoryException
public static final synchronized com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory.getInstance(String) throws com.sun.org.apache.xerces.internal.impl.dv.DVFactoryException
public final synchronized void com.sun.org.apache.xerces.internal.util.XMLCatalogResolver.clear()
public final synchronized String[] com.sun.org.apache.xerces.internal.util.XMLCatalogResolver.getCatalogList()
public final synchronized String com.sun.org.apache.xerces.internal.util.XMLCatalogResolver.resolvePublic(String,String) throws java.io.IOException
public final synchronized String com.sun.org.apache.xerces.internal.util.XMLCatalogResolver.resolveSystem(String) throws java.io.IOException
public final synchronized String com.sun.org.apache.xerces.internal.util.XMLCatalogResolver.resolveURI(String) throws java.io.IOException
public final synchronized void com.sun.org.apache.xerces.internal.util.XMLCatalogResolver.setCatalogList(String[])
public static final synchronized boolean com.sun.org.apache.xml.internal.security.Init.isInitialized()
public final synchronized String com.sun.org.glassfish.external.statistics.impl.TimeStatisticImpl.toString()
public final synchronized Object java.awt.Toolkit.getDesktopProperty(String)
public final synchronized void java.awt.geom.Path2D.closePath()
public final synchronized java.awt.Shape java.awt.geom.Path2D.createTransformedShape(java.awt.geom.AffineTransform)
public final synchronized void java.awt.geom.Path2D.Double.curveTo(double,double,double,double,double,double)
public final synchronized void java.awt.geom.Path2D.Float.curveTo(double,double,double,double,double,double)
public final synchronized void java.awt.geom.Path2D.Float.curveTo(float,float,float,float,float,float)
public final synchronized java.awt.geom.Rectangle2D java.awt.geom.Path2D.Double.getBounds2D()
public final synchronized java.awt.geom.Rectangle2D java.awt.geom.Path2D.Float.getBounds2D()
public final synchronized java.awt.geom.Point2D java.awt.geom.Path2D.getCurrentPoint()
public final synchronized int java.awt.geom.Path2D.getWindingRule()
public final synchronized void java.awt.geom.Path2D.Double.lineTo(double,double)
public final synchronized void java.awt.geom.Path2D.Float.lineTo(double,double)
public final synchronized void java.awt.geom.Path2D.Float.lineTo(float,float)
public final synchronized void java.awt.geom.Path2D.Double.moveTo(double,double)
public final synchronized void java.awt.geom.Path2D.Float.moveTo(double,double)
public final synchronized void java.awt.geom.Path2D.Float.moveTo(float,float)
public final synchronized void java.awt.geom.Path2D.Double.quadTo(double,double,double,double)
public final synchronized void java.awt.geom.Path2D.Float.quadTo(double,double,double,double)
public final synchronized void java.awt.geom.Path2D.Float.quadTo(float,float,float,float)
public final synchronized void java.awt.geom.Path2D.reset()
public final synchronized void java.beans.ChangeListenerMap<L>.add(String,L extends java.util.EventListener)
public final synchronized L extends java.util.EventListener[] java.beans.ChangeListenerMap<L>.get(String)
public final synchronized L extends java.util.EventListener[] java.beans.ChangeListenerMap<L>.getListeners()
public final synchronized boolean java.beans.ChangeListenerMap<L>.hasListeners(String)
public final synchronized void java.beans.ChangeListenerMap<L>.remove(String,L extends java.util.EventListener)
public final synchronized int javax.management.monitor.Monitor.ObservedObject.getAlreadyNotified()
public final synchronized Object javax.management.monitor.Monitor.ObservedObject.getDerivedGauge()
public final synchronized Number javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.getDerivedGaugeExceeded()
public final synchronized long javax.management.monitor.Monitor.ObservedObject.getDerivedGaugeTimeStamp()
public final synchronized boolean javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.getDerivedGaugeValid()
public final synchronized boolean javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.getDerivedGaugeValid()
public final synchronized boolean javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.getEventAlreadyNotified()
public final synchronized boolean javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.getModulusExceeded()
public final synchronized Number javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.getPreviousScanCounter()
public final synchronized Number javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.getPreviousScanGauge()
public final synchronized int javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.getStatus()
public final synchronized int javax.management.monitor.StringMonitor.StringMonitorObservedObject.getStatus()
public final synchronized Number javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.getThreshold()
public final synchronized javax.management.monitor.Monitor.NumericalType javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.getType()
public final synchronized javax.management.monitor.Monitor.NumericalType javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.getType()
public final synchronized void javax.management.monitor.Monitor.ObservedObject.setAlreadyNotified(int)
public final synchronized void javax.management.monitor.Monitor.ObservedObject.setDerivedGauge(Object)
public final synchronized void javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.setDerivedGaugeExceeded(Number)
public final synchronized void javax.management.monitor.Monitor.ObservedObject.setDerivedGaugeTimeStamp(long)
public final synchronized void javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.setDerivedGaugeValid(boolean)
public final synchronized void javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.setDerivedGaugeValid(boolean)
public final synchronized void javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.setEventAlreadyNotified(boolean)
public final synchronized void javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.setModulusExceeded(boolean)
public final synchronized void javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.setPreviousScanCounter(Number)
public final synchronized void javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.setPreviousScanGauge(Number)
public final synchronized void javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.setStatus(int)
public final synchronized void javax.management.monitor.StringMonitor.StringMonitorObservedObject.setStatus(int)
public final synchronized void javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.setThreshold(Number)
public final synchronized void javax.management.monitor.CounterMonitor.CounterMonitorObservedObject.setType(javax.management.monitor.Monitor.NumericalType)
public final synchronized void javax.management.monitor.GaugeMonitor.GaugeMonitorObservedObject.setType(javax.management.monitor.Monitor.NumericalType)
public final synchronized void javax.swing.text.AbstractDocument.readLock()
public final synchronized void javax.swing.text.AbstractDocument.readUnlock()
public final synchronized long jdk.management.resource.BoundedMeter.getBound()
public final synchronized long jdk.management.resource.NotifyingMeter.getGranularity()
public final synchronized long jdk.management.resource.ThrottledMeter.getRatePerSec()
public final synchronized long jdk.management.resource.BoundedMeter.setBound(long)
public final synchronized long jdk.management.resource.ThrottledMeter.setRatePerSec(long)
public static final synchronized sun.corba.Bridge sun.corba.Bridge.get()
public static final synchronized int sun.java2d.loops.GraphicsPrimitive.makePrimTypeID()
public static final synchronized int sun.java2d.loops.CompositeType.makeUniqueID(String)
public static final synchronized int sun.java2d.loops.GraphicsPrimitive.makeUniqueID(int,sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType)
public static final synchronized int sun.java2d.loops.SurfaceType.makeUniqueID(String)
public final synchronized java.awt.peer.MouseInfoPeer sun.lwawt.LWToolkit.getMouseInfoPeer()
public final synchronized java.util.Enumeration<E> sun.misc.Queue<T>.elements()
public final synchronized void sun.misc.Lock.lock() throws InterruptedException
public final synchronized java.util.Enumeration<E> sun.misc.Queue<T>.reverseElements()
public final synchronized void sun.misc.Lock.unlock()
public final synchronized void sun.security.jgss.TokenTracker.getProps(int,org.ietf.jgss.MessageProp)
public final transient synchronized void sun.swing.AccumulativeRunnable<T>.add(T[])
public static final synchronized sun.text.normalizer.UBiDiProps sun.text.normalizer.UBiDiProps.getDummy()
public static final synchronized sun.text.normalizer.UBiDiProps sun.text.normalizer.UBiDiProps.getSingleton() throws java.io.IOException
$
```
```shell
$ juggle abstract '(Number)'       
public abstract double Number.doubleValue()
public abstract float Number.floatValue()
public abstract int Number.intValue()
public abstract long Number.longValue()
$
```

In JDKs before Java 17 (when `strictfp` became the default implementation),
we find a handful of methods where it's explicitly specified:
```shell
$ juggle strictfp
public static strictfp double StrictMath.toDegrees(double)
public static strictfp double StrictMath.toRadians(double)
$
```

And here are all the `final native` methods returning a `boolean`:
```shell
$ juggle final native boolean
public final native boolean Thread.isAlive()
public final native boolean sun.misc.Unsafe.compareAndSwapInt(Object,long,int,int)
public final native boolean sun.misc.Unsafe.compareAndSwapLong(Object,long,long,long)
public final native boolean sun.misc.Unsafe.compareAndSwapObject(Object,long,Object,Object)
$
```

## Return type

Here's a modified example from `README.md`:
```shell
$ juggle -i java.net Inet6Address
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
$
```
The above is an exact type match.

When using the `-r` option, we are implicitly setting an upper bound of the return type. Using the declaration
syntax we need to be specific if we want to use an upper bound.

For example, here are all the methods named `getByAddress` that return an `InetAddress` or one of its subclasses:
```shell
$ juggle -i java.net '? extends InetAddress getByAddress' 
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
$
```

If we omit the `? extends` wildcard, we only get the methods that return exactly that type: 
```shell
$ juggle -i java.net InetAddress getByAddress 
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
$
```

We can specify lower bounds instead.  For example, which `getByAddress` methods return an `Inet6Address` or one
of its superclasses?
```shell
$ juggle -i java.net \? super Inet6Address getByAddress 
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
$
```

Because classes can implement multiple interfaces, it's possible to specify multiple lower bounds.
How do I get hold of an instance of a class that implements both the `List` and `Queue` interfaces?
```shell
$ juggle -i java.util \? extends Queue \& List
public LinkedList<E>.<init>()
public LinkedList<E>.<init>(Collection<E>)
public sun.awt.util.IdentityLinkedList<E>.<init>()
public sun.awt.util.IdentityLinkedList<E>.<init>(Collection<E>)
public LinkedList<E> sun.misc.JarIndex.get(String)
public LinkedList<E> sun.security.provider.PolicyParser.GrantEntry.principals
$
```

(Note that multiple lower bounds a separated by a `&` character, just like in Java declarations.  And of course
being a shell metacharacter this likely needs escaping in your shell.)

Finally, a question mark on its own represents an unbounded wildcard type.  Unlike in Java this also matches the 
`void` type and is equivalent to omitting an `-r` option.

Here are all the methods called `checkAccess`:
```shell
$ juggle /checkAccess/
public final void Thread.checkAccess()
public final void ThreadGroup.checkAccess()
public void SecurityManager.checkAccess(Thread)
public void SecurityManager.checkAccess(ThreadGroup)
public abstract void com.sun.jmx.snmp.internal.SnmpAccessControlModel.checkAccess(int,String,int,int,int,byte[],com.sun.jmx.snmp.SnmpOid) throws com.sun.jmx.snmp.SnmpStatusException
public abstract void com.sun.jmx.snmp.internal.SnmpAccessControlSubSystem.checkAccess(int,String,int,int,int,byte[],com.sun.jmx.snmp.SnmpOid) throws com.sun.jmx.snmp.SnmpStatusException,com.sun.jmx.snmp.SnmpUnknownAccContrModelException
public static String com.sun.org.apache.xalan.internal.utils.SecuritySupport.checkAccess(String,String,String) throws java.io.IOException
public static String com.sun.org.apache.xerces.internal.utils.SecuritySupport.checkAccess(String,String,String) throws java.io.IOException
public abstract boolean java.io.FileSystem.checkAccess(java.io.File,int)
public native boolean java.io.UnixFileSystem.checkAccess(java.io.File,int)
public abstract transient void java.nio.file.spi.FileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public void java.util.logging.LogManager.checkAccess() throws SecurityException
public synchronized void sun.applet.AppletSecurity.checkAccess(ThreadGroup)
public void sun.applet.AppletSecurity.checkAccess(Thread)
public transient void sun.nio.fs.UnixFileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public static void sun.rmi.registry.RegistryImpl.checkAccess(String) throws java.rmi.AccessException
public static void sun.swing.SwingUtilities2.checkAccess(int)
$
```

And here they are again, specifying an unbounded return type:
```shell
$ juggle \? checkAccess
public final void Thread.checkAccess()
public final void ThreadGroup.checkAccess()
public void SecurityManager.checkAccess(Thread)
public void SecurityManager.checkAccess(ThreadGroup)
public abstract void com.sun.jmx.snmp.internal.SnmpAccessControlModel.checkAccess(int,String,int,int,int,byte[],com.sun.jmx.snmp.SnmpOid) throws com.sun.jmx.snmp.SnmpStatusException
public abstract void com.sun.jmx.snmp.internal.SnmpAccessControlSubSystem.checkAccess(int,String,int,int,int,byte[],com.sun.jmx.snmp.SnmpOid) throws com.sun.jmx.snmp.SnmpStatusException,com.sun.jmx.snmp.SnmpUnknownAccContrModelException
public static String com.sun.org.apache.xalan.internal.utils.SecuritySupport.checkAccess(String,String,String) throws java.io.IOException
public static String com.sun.org.apache.xerces.internal.utils.SecuritySupport.checkAccess(String,String,String) throws java.io.IOException
public abstract boolean java.io.FileSystem.checkAccess(java.io.File,int)
public native boolean java.io.UnixFileSystem.checkAccess(java.io.File,int)
public abstract transient void java.nio.file.spi.FileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public void java.util.logging.LogManager.checkAccess() throws SecurityException
public synchronized void sun.applet.AppletSecurity.checkAccess(ThreadGroup)
public void sun.applet.AppletSecurity.checkAccess(Thread)
public transient void sun.nio.fs.UnixFileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public static void sun.rmi.registry.RegistryImpl.checkAccess(String) throws java.rmi.AccessException
public static void sun.swing.SwingUtilities2.checkAccess(int)
$
```

### Arrays

Are there any functions that return an array of arrays of `String`s?
```shell
$ juggle 'String[][]'           
public String[][] java.applet.Applet.getParameterInfo()
public String[][] java.text.DateFormatSymbols.getZoneStrings()
public String[][] javax.security.auth.PrivateCredentialPermission.getPrincipals()
public static String[][] sun.util.locale.provider.TimeZoneNameUtility.getZoneStrings(java.util.Locale)
$
```

This can also be expressed using an ellipsis (even though that's not valid Java):
```shell
$ juggle 'String[]...'           
public String[][] java.applet.Applet.getParameterInfo()
public String[][] java.text.DateFormatSymbols.getZoneStrings()
public String[][] javax.security.auth.PrivateCredentialPermission.getPrincipals()
public static String[][] sun.util.locale.provider.TimeZoneNameUtility.getZoneStrings(java.util.Locale)
$
```

## Matching member names

The new syntax lets us match members by name, either literal match
or by regular expression.

Here's what the old syntax allowed: a case-insensitive literal match:
```shell
$ juggle /isjavaletterordigit/i
public static boolean Character.isJavaLetterOrDigit(char)
$
```

(There's an ambiguity in the raw grammar: an IDENT on its own might
be interpreted either as a return type or a member name.  In time
Juggle will resolve this by favouring a return type if a type of
that name can be found. For now though, I'm forcing the parser's
hand by including a type of `?`.)

Just dropping the `-n` adopts the new syntax, but literals are matched
case-sensitively, so our first attempt fails: 
```shell
$ juggle '? isjavaletterordigit'
$
```

Getting the case right works:
```shell
$ juggle '? isJavaLetterOrDigit'
public static boolean Character.isJavaLetterOrDigit(char)
$
```

We can switch to using a regular expression by surrounding in `/`
characters. REs aren't to the ends of the member, so this matches
two methods:
```shell
$ juggle /isJavaLetter/
public static boolean Character.isJavaLetter(char)
public static boolean Character.isJavaLetterOrDigit(char)
$
```

Using `^` and `$` ties it. (Note: both of these characters are
usually interpreted by the shell, so we additionally need quote
marks.)
```shell
$ juggle '/^isJavaLetter$/'
public static boolean Character.isJavaLetter(char)
$
```

Adding a `i` after the closing `/` makes the match case-insensitive.
Combined with the anchors this gives us the same as the old `-n`.
```shell
$ juggle '/^isjavaletterordigit$/i'
public static boolean Character.isJavaLetterOrDigit(char)
$
```

## Parameters

Are there any functions that take twenty parameters? Juggle knows (19 commas 
separate 20 parameters) ...
```shell
$ juggle "(,,,,,,,,,,,,,,,,,,,)"
$
```

If `(,)` shows methods with two unknown parameters, what does `()` show?
```shell
$ juggle 'Thread ()'
public static native Thread Thread.currentThread()
public Thread.<init>()
public com.sun.corba.se.impl.javax.rmi.CORBA.KeepAlive.<init>()
public static final sun.audio.AudioPlayer sun.audio.AudioPlayer.player
public sun.java2d.loops.GraphicsPrimitive.TraceReporter.<init>()
$
```
Answer: it shows methods with no parameters.  This feels natural, but does
raise the question of how to show methods with a single parameter.

The solution is to use an explicit wildcard, `(?)`:
```shell
$ juggle 'Thread (?)'
public Thread.<init>(Runnable)
public Thread.<init>(String)
public final Thread java.util.concurrent.locks.AbstractQueuedLongSynchronizer.getFirstQueuedThread()
public final Thread java.util.concurrent.locks.AbstractQueuedSynchronizer.getFirstQueuedThread()
public Thread sun.applet.AppletPanel.getAppletHandlerThread()
public Thread sun.awt.AppContext.CreateThreadAction.run()
public static Thread sun.misc.InnocuousThread.newSystemThread(Runnable)
public Thread sun.rmi.runtime.NewThreadAction.run()
public com.sun.corba.se.impl.transport.SelectorImpl.<init>(com.sun.corba.se.spi.orb.ORB)
public com.sun.jmx.snmp.tasks.ThreadService.ExecutorThread.<init>(com.sun.jmx.snmp.tasks.ThreadService)
public sun.misc.InnocuousThread.<init>(Runnable)
$
```

Which methods meet the general contract of the `Comparator` interface?
```shell
$ juggle "int (?,? extends Object, ? extends Object)"
public volatile int String.CaseInsensitiveComparator.compare(Object,Object)
public int com.sun.corba.se.impl.io.ObjectStreamClass.CompareClassByName.compare(Object,Object)
public int com.sun.corba.se.impl.io.ObjectStreamClass.CompareMemberByName.compare(Object,Object)
public int com.sun.corba.se.impl.io.ObjectStreamClass.CompareObjStrFieldsByName.compare(Object,Object)
public int com.sun.corba.se.impl.io.ObjectStreamClass.MethodSignature.compare(Object,Object)
public int com.sun.corba.se.impl.orbutil.ObjectStreamClassUtil_1_3.CompareClassByName.compare(Object,Object)
public int com.sun.corba.se.impl.orbutil.ObjectStreamClassUtil_1_3.CompareMemberByName.compare(Object,Object)
public int com.sun.corba.se.impl.orbutil.ObjectStreamClassUtil_1_3.MethodSignature.compare(Object,Object)
public int com.sun.corba.se.impl.orbutil.ObjectStreamClass_1_3_1.CompareClassByName.compare(Object,Object)
public int com.sun.corba.se.impl.orbutil.ObjectStreamClass_1_3_1.CompareMemberByName.compare(Object,Object)
public int com.sun.corba.se.impl.orbutil.ObjectStreamClass_1_3_1.MethodSignature.compare(Object,Object)
public volatile int com.sun.jmx.mbeanserver.MBeanAnalyzer.MethodOrder.compare(Object,Object)
public volatile int com.sun.media.sound.ModelInstrumentComparator.compare(Object,Object)
public volatile int com.sun.media.sound.SoftPerformer.KeySortComparator.compare(Object,Object)
public final int com.sun.org.apache.xerces.internal.impl.dv.xs.DecimalDV.compare(Object,Object)
public int com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV.compare(Object,Object)
public int com.sun.org.apache.xerces.internal.impl.dv.xs.DoubleDV.compare(Object,Object)
public int com.sun.org.apache.xerces.internal.impl.dv.xs.FloatDV.compare(Object,Object)
public int com.sun.org.apache.xerces.internal.impl.dv.xs.PrecisionDecimalDV.compare(Object,Object)
public int com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator.compare(Object,Object)
public volatile int com.sun.org.apache.xml.internal.security.c14n.helper.AttrCompare.compare(Object,Object)
public volatile int com.sun.xml.internal.bind.v2.model.impl.ClassInfoImpl.PropertySorter.compare(Object,Object)
public volatile int com.sun.xml.internal.ws.transport.Headers.InsensitiveComparator.compare(Object,Object)
public int java.awt.datatransfer.DataFlavor.TextFlavorComparator.compare(Object,Object)
public volatile int java.net.CookieManager.CookiePathComparator.compare(Object,Object)
public int java.text.Collator.compare(Object,Object)
public abstract int java.util.Comparator<T>.compare(T,T)
public int java.util.Arrays.NaturalOrder.compare(Object,Object)
public volatile int java.util.Collections.ReverseComparator.compare(Object,Object)
public int java.util.Collections.ReverseComparator2<T>.compare(T,T)
public volatile int java.util.Comparators.NaturalOrderComparator.compare(Object,Object)
public int java.util.Comparators.NullComparator<T>.compare(T,T)
public abstract int java.util.function.ToIntBiFunction<T,U>.applyAsInt(T,U)
public volatile int javax.swing.CompareTabOrderComparator.compare(Object,Object)
public volatile int javax.swing.LayoutComparator.compare(Object,Object)
public volatile int javax.swing.plaf.basic.BasicTreeUI.TreeTransferHandler.compare(Object,Object)
public int javax.swing.table.TableRowSorter.ComparableComparator.compare(Object,Object)
public abstract int javax.swing.tree.TreeModel.getIndexOfChild(Object,Object)
public int javax.swing.tree.DefaultTreeModel.getIndexOfChild(Object,Object)
public int sun.awt.datatransfer.DataTransferer.CharsetComparator.compare(Object,Object)
public int sun.awt.datatransfer.DataTransferer.DataFlavorComparator.compare(Object,Object)
public int sun.awt.datatransfer.DataTransferer.IndexOrderComparator.compare(Object,Object)
public int sun.java2d.Spans.SpanIntersection.compare(Object,Object)
public volatile int sun.management.DiagnosticCommandImpl.OperationInfoComparator.compare(Object,Object)
public volatile int sun.misc.ASCIICaseInsensitiveComparator.compare(Object,Object)
public volatile int sun.security.provider.certpath.ForwardBuilder.PKIXCertComparator.compare(Object,Object)
public volatile int sun.security.provider.certpath.PKIX.CertStoreComparator.compare(Object,Object)
public volatile int sun.security.util.ByteArrayLexOrder.compare(Object,Object)
public volatile int sun.security.util.ByteArrayTagOrder.compare(Object,Object)
public volatile int sun.security.x509.AVAComparator.compare(Object,Object)
public volatile int sun.swing.FilePane.DirectoriesFirstComparatorWrapper.compare(Object,Object)
public volatile int sun.util.locale.provider.CalendarNameProviderImpl.LengthBasedComparator.compare(Object,Object)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<T>)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public static <T> int java.util.Objects.compare(T,T,java.util.Comparator<T>)
public final int java.util.concurrent.atomic.AtomicIntegerFieldUpdater<T>.getAndUpdate(T,java.util.function.IntUnaryOperator)
public final int java.util.concurrent.atomic.AtomicIntegerFieldUpdater<T>.updateAndGet(T,java.util.function.IntUnaryOperator)
public int javax.swing.UIDefaults.getInt(Object,java.util.Locale)
public int String.CaseInsensitiveComparator.compare(String,String)
public int com.sun.corba.se.impl.activation.ServerManagerImpl.getServerPortForType(com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB,String) throws com.sun.corba.se.spi.activation.NoSuchEndPoint
public abstract int com.sun.corba.se.spi.activation.LocatorOperations.getServerPortForType(com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB,String) throws com.sun.corba.se.spi.activation.NoSuchEndPoint
public int com.sun.corba.se.spi.activation._LocatorStub.getServerPortForType(com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB,String) throws com.sun.corba.se.spi.activation.NoSuchEndPoint
public int com.sun.corba.se.spi.activation._ServerManagerStub.getServerPortForType(com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB,String) throws com.sun.corba.se.spi.activation.NoSuchEndPoint
public int com.sun.java.util.jar.pack.Coding.readFrom(byte[],int[])
public int com.sun.jmx.mbeanserver.MBeanAnalyzer.MethodOrder.compare(java.lang.reflect.Method,java.lang.reflect.Method)
public abstract int com.sun.jmx.snmp.internal.SnmpMsgProcessingModel.encode(com.sun.jmx.snmp.internal.SnmpDecryptedPdu,byte[]) throws com.sun.jmx.snmp.SnmpTooBigException
public int com.sun.media.sound.ModelInstrumentComparator.compare(javax.sound.midi.Instrument,javax.sound.midi.Instrument)
public int com.sun.media.sound.SoftPerformer.KeySortComparator.compare(com.sun.media.sound.ModelSource,com.sun.media.sound.ModelSource)
public abstract int com.sun.nio.sctp.SctpChannel.send(java.nio.ByteBuffer,com.sun.nio.sctp.MessageInfo) throws java.io.IOException
public abstract int com.sun.nio.sctp.SctpMultiChannel.send(java.nio.ByteBuffer,com.sun.nio.sctp.MessageInfo) throws java.io.IOException
public int com.sun.org.apache.bcel.internal.generic.ConstantPoolGen.addConstant(com.sun.org.apache.bcel.internal.classfile.Constant,com.sun.org.apache.bcel.internal.generic.ConstantPoolGen)
public int com.sun.org.apache.bcel.internal.generic.ConstantPoolGen.addNameAndType(String,String)
public static int com.sun.org.apache.bcel.internal.generic.MethodGen.getMaxStack(com.sun.org.apache.bcel.internal.generic.ConstantPoolGen,com.sun.org.apache.bcel.internal.generic.InstructionList,com.sun.org.apache.bcel.internal.generic.CodeExceptionGen[])
public int com.sun.org.apache.bcel.internal.generic.ConstantPoolGen.lookupNameAndType(String,String)
public <E> int com.sun.org.apache.xalan.internal.utils.FeaturePropertyBase.getIndex(Class<T>,String)
public int com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl.createDeferredElement(String,String)
public int com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl.createDeferredEntityReference(String,String)
public int com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl.createDeferredProcessingInstruction(String,String)
public int com.sun.org.apache.xerces.internal.dom.DOMNormalizer.XMLAttributesProxy.getIndex(String,String)
public int com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser.AttributesProxy.getIndex(String,String)
public int com.sun.org.apache.xerces.internal.util.AttributesProxy.getIndex(String,String)
public int com.sun.org.apache.xerces.internal.util.XMLAttributesImpl.getIndex(String,String)
public int com.sun.org.apache.xerces.internal.util.XMLAttributesImpl.getIndexFast(String,String)
public abstract int com.sun.org.apache.xerces.internal.xni.XMLAttributes.getIndex(String,String)
public int com.sun.org.apache.xml.internal.security.c14n.helper.AttrCompare.compare(org.w3c.dom.Attr,org.w3c.dom.Attr)
public int com.sun.org.apache.xml.internal.security.utils.ElementProxy.length(String,String)
public final int com.sun.org.apache.xml.internal.serializer.AttributesImplSerializer.getIndex(String,String)
public int com.sun.org.apache.xml.internal.utils.AttList.getIndex(String,String)
public int com.sun.org.apache.xpath.internal.NodeSet.addNodeInDocOrder(org.w3c.dom.Node,com.sun.org.apache.xpath.internal.XPathContext)
public int com.sun.org.apache.xpath.internal.compiler.FunctionTable.installFunction(String,Class<T>)
public int com.sun.xml.internal.bind.util.AttributesImpl.getIndex(String,String)
public int com.sun.xml.internal.bind.util.AttributesImpl.getIndexFast(String,String)
public int com.sun.xml.internal.bind.v2.model.impl.ClassInfoImpl.PropertySorter.compare(com.sun.xml.internal.bind.v2.model.impl.PropertyInfoImpl<T,C,F,M>,com.sun.xml.internal.bind.v2.model.impl.PropertyInfoImpl<T,C,F,M>)
public abstract int com.sun.xml.internal.bind.v2.runtime.NamespaceContext2.force(String,String)
public int com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl.force(String,String)
public int com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl.put(String,String)
public int com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor.AttributesImpl.getIndex(String,String)
public final int com.sun.xml.internal.fastinfoset.sax.AttributesHolder.getIndex(String,String)
public final int com.sun.xml.internal.org.jvnet.fastinfoset.sax.helpers.EncodingAlgorithmAttributesImpl.getIndex(String,String)
public final int com.sun.xml.internal.stream.buffer.AttributesHolder.getIndex(String,String)
public int com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter.newNameType(String,String)
public abstract int com.sun.xml.internal.ws.streaming.Attributes.getIndex(String,String)
public int com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.AttributesImpl.getIndex(String,String)
public int com.sun.xml.internal.ws.transport.Headers.InsensitiveComparator.compare(String,String)
public int java.awt.Component.checkImage(java.awt.Image,java.awt.image.ImageObserver)
public abstract int java.io.FileSystem.compare(java.io.File,java.io.File)
public int java.io.UnixFileSystem.compare(java.io.File,java.io.File)
public int java.net.CookieManager.CookiePathComparator.compare(java.net.HttpCookie,java.net.HttpCookie)
public abstract int java.nio.channels.DatagramChannel.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public abstract int java.nio.file.attribute.UserDefinedFileAttributeView.read(String,java.nio.ByteBuffer) throws java.io.IOException
public abstract int java.nio.file.attribute.UserDefinedFileAttributeView.write(String,java.nio.ByteBuffer) throws java.io.IOException
public abstract int java.sql.Statement.executeUpdate(String,int[]) throws java.sql.SQLException
public abstract int java.sql.Statement.executeUpdate(String,String[]) throws java.sql.SQLException
public abstract int java.text.Collator.compare(String,String)
public synchronized int java.text.RuleBasedCollator.compare(String,String)
public int java.util.Collections.ReverseComparator.compare(Comparable<T>,Comparable<T>)
public int java.util.Comparators.NaturalOrderComparator.compare(Comparable<T>,Comparable<T>)
public int java.util.Base64.Decoder.decode(byte[],byte[])
public int java.util.Base64.Encoder.encode(byte[],byte[])
public abstract int javax.imageio.metadata.IIOMetadataFormat.getAttributeDataType(String,String)
public int javax.imageio.metadata.IIOMetadataFormatImpl.getAttributeDataType(String,String)
public abstract int javax.imageio.metadata.IIOMetadataFormat.getAttributeListMaxLength(String,String)
public int javax.imageio.metadata.IIOMetadataFormatImpl.getAttributeListMaxLength(String,String)
public abstract int javax.imageio.metadata.IIOMetadataFormat.getAttributeListMinLength(String,String)
public int javax.imageio.metadata.IIOMetadataFormatImpl.getAttributeListMinLength(String,String)
public abstract int javax.imageio.metadata.IIOMetadataFormat.getAttributeValueType(String,String)
public int javax.imageio.metadata.IIOMetadataFormatImpl.getAttributeValueType(String,String)
public abstract int javax.smartcardio.CardChannel.transmit(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.smartcardio.CardException
public static int javax.sound.sampled.AudioSystem.write(javax.sound.sampled.AudioInputStream,javax.sound.sampled.AudioFileFormat.Type,java.io.File) throws java.io.IOException
public static int javax.sound.sampled.AudioSystem.write(javax.sound.sampled.AudioInputStream,javax.sound.sampled.AudioFileFormat.Type,java.io.OutputStream) throws java.io.IOException
public int javax.swing.CompareTabOrderComparator.compare(java.awt.Component,java.awt.Component)
public int javax.swing.LayoutComparator.compare(java.awt.Component,java.awt.Component)
public int javax.swing.JFileChooser.showDialog(java.awt.Component,String) throws java.awt.HeadlessException
public abstract int javax.swing.plaf.TreeUI.getRowForPath(javax.swing.JTree,javax.swing.tree.TreePath)
public abstract int javax.swing.plaf.ListUI.locationToIndex(javax.swing.JList<E>,java.awt.Point)
public abstract int javax.swing.plaf.TextUI.viewToModel(javax.swing.text.JTextComponent,java.awt.Point)
public int javax.swing.plaf.basic.BasicTreeUI.TreeTransferHandler.compare(javax.swing.tree.TreePath,javax.swing.tree.TreePath)
public int javax.swing.plaf.basic.BasicTreeUI.getRowForPath(javax.swing.JTree,javax.swing.tree.TreePath)
public int javax.swing.plaf.basic.BasicListUI.locationToIndex(javax.swing.JList<E>,java.awt.Point)
public int javax.swing.plaf.basic.BasicTextUI.viewToModel(javax.swing.text.JTextComponent,java.awt.Point)
public int javax.swing.plaf.multi.MultiTreeUI.getRowForPath(javax.swing.JTree,javax.swing.tree.TreePath)
public int javax.swing.plaf.multi.MultiListUI.locationToIndex(javax.swing.JList<E>,java.awt.Point)
public int javax.swing.plaf.multi.MultiTextUI.viewToModel(javax.swing.text.JTextComponent,java.awt.Point)
public int jdk.internal.instrumentation.MaxLocalsTracker.getMaxLocals(String,String)
public int jdk.internal.org.objectweb.asm.ClassWriter.newNameType(String,String)
public abstract int jdk.internal.org.xml.sax.Attributes.getIndex(String,String)
public int jdk.internal.util.xml.impl.Attrs.getIndex(String,String)
public int jdk.management.resource.internal.inst.DatagramChannelImplRMHooks.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public abstract int org.ietf.jgss.GSSContext.initSecContext(java.io.InputStream,java.io.OutputStream) throws org.ietf.jgss.GSSException
public abstract int org.xml.sax.Attributes.getIndex(String,String)
public int org.xml.sax.helpers.AttributesImpl.getIndex(String,String)
public int org.xml.sax.helpers.ParserAdapter.AttributeListAdapter.getIndex(String,String)
public int sun.awt.geom.Curve.compareTo(sun.awt.geom.Curve,double[])
public int sun.awt.geom.Edge.compareTo(sun.awt.geom.Edge,double[])
public int sun.awt.geom.Order1.compareTo(sun.awt.geom.Curve,double[])
public static native int sun.awt.image.ImagingLib.lookupByteBI(java.awt.image.BufferedImage,java.awt.image.BufferedImage,byte[][])
public static native int sun.awt.image.ImagingLib.lookupByteRaster(java.awt.image.Raster,java.awt.image.Raster,byte[][])
public static int sun.font.FontStrikeDesc.getAAHintIntVal(sun.font.Font2D,java.awt.Font,java.awt.font.FontRenderContext)
public int sun.java2d.xr.XRPaints.getGradientLength(java.awt.geom.Point2D,java.awt.geom.Point2D)
public int sun.management.DiagnosticCommandImpl.OperationInfoComparator.compare(javax.management.MBeanOperationInfo,javax.management.MBeanOperationInfo)
public native int sun.management.HotspotThread.getInternalThreadTimes0(String[],long[])
public int sun.misc.FDBigInteger.addAndCmp(sun.misc.FDBigInteger,sun.misc.FDBigInteger)
public int sun.misc.ASCIICaseInsensitiveComparator.compare(String,String)
public int sun.nio.ch.DatagramChannelImpl.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public volatile int sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public int sun.nio.ch.sctp.SctpChannelImpl.send(java.nio.ByteBuffer,com.sun.nio.sctp.MessageInfo) throws java.io.IOException
public int sun.nio.ch.sctp.SctpMultiChannelImpl.send(java.nio.ByteBuffer,com.sun.nio.sctp.MessageInfo) throws java.io.IOException
public int sun.security.jgss.GSSContextImpl.initSecContext(java.io.InputStream,java.io.OutputStream) throws org.ietf.jgss.GSSException
public int sun.security.provider.certpath.ForwardBuilder.PKIXCertComparator.compare(java.security.cert.X509Certificate,java.security.cert.X509Certificate)
public int sun.security.provider.certpath.PKIX.CertStoreComparator.compare(java.security.cert.CertStore,java.security.cert.CertStore)
public int sun.security.smartcardio.ChannelImpl.transmit(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.smartcardio.CardException
public final int sun.security.util.ByteArrayLexOrder.compare(byte[],byte[])
public final int sun.security.util.ByteArrayTagOrder.compare(byte[],byte[])
public int sun.security.x509.AVAComparator.compare(sun.security.x509.AVA,sun.security.x509.AVA)
public int sun.swing.FilePane.DirectoriesFirstComparatorWrapper.compare(java.io.File,java.io.File)
public static int sun.swing.DefaultLookup.getInt(javax.swing.JComponent,javax.swing.plaf.ComponentUI,String)
public static int sun.swing.SwingUtilities2.getLeftSideBearing(javax.swing.JComponent,java.awt.FontMetrics,String)
public static int sun.swing.SwingUtilities2.stringWidth(javax.swing.JComponent,java.awt.FontMetrics,String)
public int sun.util.locale.provider.CalendarNameProviderImpl.LengthBasedComparator.compare(String,String)
$
```
A fair few! Note that not all of these really qualify though, since to qualify
as a `Comparator` lambda, the method needs to be the only one in its class.
Juggle can't tell you that.

Lower type bounds come into their own with parameter queries.  Imagine I have a
`Inet6Address`. What methods can I use to get a `NetworkInterface` from it?
```shell
$ juggle -i java.net NetworkInterface (? super Inet6Address)
public NetworkInterface Inet6Address.getScopedInterface()
public static NetworkInterface NetworkInterface.getByInetAddress(InetAddress) throws SocketException
$
```

## Exceptions

If we don't specify a `throws` clause, Juggle shows members that throw along
with those that don't:
```shell
$ juggle 'int (String,int)'
public int String.codePointAt(int)
public int String.codePointBefore(int)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int sun.text.normalizer.UTF16.charAt(String,int)
public static int sun.text.normalizer.Utility.skipWhitespace(String,int)
public char String.charAt(int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static byte Byte.parseByte(String,int) throws NumberFormatException
public static short Short.parseShort(String,int) throws NumberFormatException
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.misc.PerformanceLogger.setTime(String,long)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public abstract char CharSequence.charAt(int)
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static Integer sun.net.NetProperties.getInteger(String,int)
public volatile int String.compareTo(Object)
public static Byte Byte.valueOf(String,int) throws NumberFormatException
public static Short Short.valueOf(String,int) throws NumberFormatException
public abstract int Comparable<T>.compareTo(T)
public static Integer Integer.getInteger(String,Integer)
$
```

If we specify the `throws` keyword but don't follow it with any classes,
Juggle only lists the members that _don't_ throw any types:
```shell
$ juggle 'int (String,int) throws'
public int String.codePointAt(int)
public int String.codePointBefore(int)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int sun.text.normalizer.UTF16.charAt(String,int)
public static int sun.text.normalizer.Utility.skipWhitespace(String,int)
public char String.charAt(int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.misc.PerformanceLogger.setTime(String,long)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public abstract char CharSequence.charAt(int)
public static Integer Integer.getInteger(String,int)
public static Integer sun.net.NetProperties.getInteger(String,int)
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static Integer Integer.getInteger(String,Integer)
$
```

Conversely, specifying a type after the `throws` only lists members that throw
that particular type:
```shell
$ juggle 'int (String,int) throws NumberFormatException'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static byte Byte.parseByte(String,int) throws NumberFormatException
public static short Short.parseShort(String,int) throws NumberFormatException
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static Byte Byte.valueOf(String,int) throws NumberFormatException
public static Short Short.valueOf(String,int) throws NumberFormatException
$
```

As with return and parameter types, exception types are now matched precisely
if we don't specify any bounded wildcards in the query:
```shell
$ juggle 'int (String,int) throws RuntimeException'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static byte Byte.parseByte(String,int) throws NumberFormatException
public static short Short.parseShort(String,int) throws NumberFormatException
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static Byte Byte.valueOf(String,int) throws NumberFormatException
public static Short Short.valueOf(String,int) throws NumberFormatException
$
```

If we use `-c none` to prevent conversions, we don't see any results:
```shell
$ juggle -c none 'int (String,int) throws RuntimeException'
$
```

But we can use an upper bound if we wanted to match any class that is lower in
the exception hierarchy:
```shell
$ juggle 'int (String,int) throws ? extends NumberFormatException'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
$
```
Because we've used a bounded wildcard in the query, Juggle doesn't perform any
conversions for us (so we don't show results where the return type would've
required a Widening Primitive conversion, e.g. `byte Byte.parseByte(String)`.)

Or even a wildcard if we don't care what class might be thrown:
```shell
$ juggle 'int (String,int) throws ?'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static byte Byte.parseByte(String,int) throws NumberFormatException
public static short Short.parseShort(String,int) throws NumberFormatException
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static Byte Byte.valueOf(String,int) throws NumberFormatException
public static Short Short.valueOf(String,int) throws NumberFormatException
$
```

Lower bounds are possible too:
```shell
$ juggle 'int (String,int) throws ? super NumberFormatException'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
$
```
