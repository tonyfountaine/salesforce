package nz.co.trineo.salesforce;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import nz.co.trineo.salesforce.model.RunTestsResult;

public class ConvertUtilsTest {

	private com.sforce.soap.apex.RunTestsResult result;

	public void initRunTestResult() throws JAXBException, XMLStreamException {
		final XMLInputFactory xif = XMLInputFactory.newFactory();
		final StreamSource xml = new StreamSource(getClass().getResourceAsStream("/testResult.xml"));
		final XMLStreamReader xsr = xif.createXMLStreamReader(xml);
		xsr.nextTag();
		while (!xsr.getLocalName().equals("result")) {
			xsr.nextTag();
		}
		final JAXBContext jaxbContext = JAXBContext.newInstance(com.sforce.soap.apex.RunTestsResult.class);
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		final JAXBElement<com.sforce.soap.apex.RunTestsResult> jb = unmarshaller.unmarshal(xsr,
				com.sforce.soap.apex.RunTestsResult.class);
		xsr.close();
		result = jb.getValue();
	}

	@Test
	public void verifyConvertingRunTestsResult() throws JAXBException, XMLStreamException {
		initRunTestResult();
		final RunTestsResult actual = ConvertUtils.toRunTestsResult(result);
		Assert.assertNotNull(actual);
		Assert.assertNotNull(actual.getApexLogId());
	}

	@Test
	public void verifyRunTestsResultContent() throws JAXBException, IOException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(com.sforce.soap.apex.RunTestsResult.class);
		final Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		final File file = new File("result.xml");
		final FileOutputStream os = new FileOutputStream(file);
		final com.sforce.soap.apex.RunTestsResult element = new com.sforce.soap.apex.RunTestsResult();
		element.setApexLogId("testApexLogId");
		final QName root = new QName("result");
		final JAXBElement<com.sforce.soap.apex.RunTestsResult> je = new JAXBElement<>(root,
				com.sforce.soap.apex.RunTestsResult.class, element);
		marshaller.marshal(je, os);
		os.close();
	}
}
