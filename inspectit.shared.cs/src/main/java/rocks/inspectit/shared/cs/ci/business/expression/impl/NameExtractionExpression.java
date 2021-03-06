package rocks.inspectit.shared.cs.ci.business.expression.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;

/**
 * This configuration element describes the dynamic extraction of a name for
 * {@link BusinessTransactionData} instance.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "name-extraction")
public class NameExtractionExpression {
	/**
	 * Postfix for business transactions derived from dynamic name extractions, where the name could
	 * not be mapped.
	 */
	public static final String UNKNOWN_DYNAMIC_BUSINESS_TRANSACTION_POSTFIX = " (unmapped)";

	/**
	 * Postfix for business transactions derived from dynamic name extractions, where the name is an
	 * empty string.
	 */
	public static final String EMPTY_DYNAMIC_BUSINESS_TRANSACTION_POSTFIX = " (empty)";

	/**
	 * Regular expression used for name extraction from target string.
	 */
	@XmlAttribute(name = "regularExpression", required = true)
	private String regularExpression;

	/**
	 * Pattern for the target name. May include dynamically extracted name fragments.
	 */
	@XmlAttribute(name = "targetNamePattern", required = true)
	private String targetNamePattern;

	/**
	 * If search-in-trace attribute is set to true, this attribute defines the maximum depth in the
	 * trace to search for the string matching. -1 means no limit.
	 */
	@XmlAttribute(name = "max-search-depth")
	private Integer maxSearchDepth = Integer.valueOf(-1);

	/**
	 * Indicates whether this value source apply to any node in the {@link InvocationSequenceData}
	 * or only at the root element. If this variable is false, the string value extraction will be
	 * only applied on the root element of the invocation sequence, otherwise the invocation
	 * sequence is searched for a corresponding node.
	 */
	@XmlAttribute(name = "search-in-trace")
	private Boolean searchNodeInTrace = Boolean.FALSE;

	/**
	 * Source of the string value to be compared against the snippet.
	 */
	@XmlElementRef(required = true)
	private StringValueSource stringValueSource;

	/**
	 * Compiled regular expression cache. This field is not going to be serialized with JAX-B.
	 */
	private transient Pattern regex;

	/**
	 * Extracts name from the passed invocation sequence.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to extract the name from
	 * @param cachedDataService
	 *            {@link ICachedDataService} used to retrieve method names.
	 * @return extracted name
	 */
	public String extractName(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		try {
			compileRegexPattern();
		} catch (PatternSyntaxException e) {
			return null;
		}

		String[] stringValues = getStringValueSource().getStringValues(invocSequence, cachedDataService);

		for (String stringValue : stringValues) {
			Matcher matcher = regex.matcher(stringValue);
			if (!matcher.matches()) {
				continue;
			}
			String resultingName = getTargetNamePattern();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String groupValue = matcher.group(i);
				resultingName = resultingName.replaceFirst("\\(" + i + "\\)", groupValue);
			}
			return resultingName;
		}

		return null;
	}

	/**
	 * Compiles the regular expression if it has not been compiled yet.
	 */
	private void compileRegexPattern() {
		if (null == regex) {
			regex = Pattern.compile(getRegularExpression());
		}
	}

	/**
	 * Gets {@link #regularExpression}.
	 *
	 * @return {@link #regularExpression}
	 */
	public String getRegularExpression() {
		return regularExpression;
	}

	/**
	 * Sets {@link #regularExpression}.
	 *
	 * @param regularExpression
	 *            New value for {@link #regularExpression}
	 */
	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}

	/**
	 * Gets {@link #targetNamePattern}.
	 *
	 * @return {@link #targetNamePattern}
	 */
	public String getTargetNamePattern() {
		return targetNamePattern;
	}

	/**
	 * Sets {@link #targetNamePattern}.
	 *
	 * @param targetNamePattern
	 *            New value for {@link #targetNamePattern}
	 */
	public void setTargetNamePattern(String targetNamePattern) {
		this.targetNamePattern = targetNamePattern;
	}

	/**
	 * Gets {@link #stringValueSource}.
	 *
	 * @return {@link #stringValueSource}
	 */
	public StringValueSource getStringValueSource() {
		return stringValueSource;
	}

	/**
	 * Sets {@link #stringValueSource}.
	 *
	 * @param stringValueSource
	 *            New value for {@link #stringValueSource}
	 */
	public void setStringValueSource(StringValueSource stringValueSource) {
		this.stringValueSource = stringValueSource;
	}

	/**
	 * Gets {@link #maxSearchDepth}.
	 *
	 * @return {@link #maxSearchDepth}
	 */
	public int getMaxSearchDepth() {
		return maxSearchDepth.intValue();
	}

	/**
	 * Sets {@link #maxSearchDepth}.
	 *
	 * @param maxSearchDepth
	 *            New value for {@link #maxSearchDepth}
	 */
	public void setMaxSearchDepth(int maxSearchDepth) {
		this.maxSearchDepth = Integer.valueOf(maxSearchDepth);
	}

	/**
	 * Gets {@link #searchNodeInTrace}.
	 *
	 * @return {@link #searchNodeInTrace}
	 */
	public boolean isSearchNodeInTrace() {
		return searchNodeInTrace.booleanValue();
	}

	/**
	 * Sets {@link #searchNodeInTrace}.
	 *
	 * @param searchNodeInTrace
	 *            New value for {@link #searchNodeInTrace}
	 */
	public void setSearchNodeInTrace(boolean searchNodeInTrace) {
		this.searchNodeInTrace = Boolean.valueOf(searchNodeInTrace);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.maxSearchDepth == null) ? 0 : this.maxSearchDepth.hashCode());
		result = (prime * result) + ((this.regularExpression == null) ? 0 : this.regularExpression.hashCode());
		result = (prime * result) + ((this.searchNodeInTrace == null) ? 0 : this.searchNodeInTrace.hashCode());
		result = (prime * result) + ((this.stringValueSource == null) ? 0 : this.stringValueSource.hashCode());
		result = (prime * result) + ((this.targetNamePattern == null) ? 0 : this.targetNamePattern.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NameExtractionExpression other = (NameExtractionExpression) obj;
		if (this.maxSearchDepth == null) {
			if (other.maxSearchDepth != null) {
				return false;
			}
		} else if (!this.maxSearchDepth.equals(other.maxSearchDepth)) {
			return false;
		}
		if (this.regularExpression == null) {
			if (other.regularExpression != null) {
				return false;
			}
		} else if (!this.regularExpression.equals(other.regularExpression)) {
			return false;
		}
		if (this.searchNodeInTrace == null) {
			if (other.searchNodeInTrace != null) {
				return false;
			}
		} else if (!this.searchNodeInTrace.equals(other.searchNodeInTrace)) {
			return false;
		}
		if (this.stringValueSource == null) {
			if (other.stringValueSource != null) {
				return false;
			}
		} else if (!this.stringValueSource.equals(other.stringValueSource)) {
			return false;
		}
		if (this.targetNamePattern == null) {
			if (other.targetNamePattern != null) {
				return false;
			}
		} else if (!this.targetNamePattern.equals(other.targetNamePattern)) {
			return false;
		}
		return true;
	}
}
