<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema" >

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Sod Tag Documentation</title>
      </head>
      <body>
        <h1>Sod Tag Documentation</h1>
        <p>
The Structure of a sod configuration file is shown below. In general, for 
each step there are multiple tags that can fufull that function, and those 
shown below are merely placeholders. In addition to the specialized subsetters,
 there are also logical subsetters for each type. For example, at the eventAttr
subsetter location, there are eventAttrAND, eventAttrOR, eventAttrXOR and 
eventAttrNOT. Each of these logical subsetters take as subtags any subsetter
of the same type allowing the creation of complex subsetters from the existing
simple ones.</p>
        <p>
Structure of a SOD configuration file. 
        <pre>
&lt;sod&gt;
    &lt;eventArm&gt;
        &lt;<a href="#eventFinderType">eventFinder</a>&gt; or &lt;<a href="#eventChannelFinderType">eventChannelFinder</a>&gt;
        &lt;<a href="#eventAttrType">eventAttr</a>&gt;
        &lt;<a href="#originType">origin</a> &gt;
        &lt;<a href="#eventProcessType">eventProcess</a>&gt;
    &lt;/eventArm&gt;
    &lt;networkArm&gt;
        &lt;<a href="#networkFinderType">networkFinder</a>&gt;
        &lt;<a href="#networkIDType">networkID</a>&gt;
        &lt;<a href="#networkAttrType">networkAttr</a>&gt;
        &lt;<a href="#stationIDType">stationID</a>&gt;
        &lt;<a href="#stationType">station</a>&gt;
        &lt;<a href="#siteIDType">siteID</a>&gt;
        &lt;<a href="#siteType">site</a>&gt;
        &lt;<a href="#channelIDType">channelID</a>&gt;
        &lt;<a href="#channelType">channel</a>&gt;
        &lt;<a href="#networkProcessType">networkProcess</a>&gt;
    &lt;/networkArm&gt;
    &lt;waveFormArm&gt;
        &lt;<a href="#eventStationType">eventStation</a>&gt;
        &lt;localSeismogramArm&gt;
            &lt;<a href="#eventChannelType">eventChannel</a>&gt;
            &lt;<a href="#requestGeneratorType">requestGenerator</a>&gt;
            &lt;<a href="#dataCenterType">dataCenter</a>&gt;
            &lt;<a href="#availableDataType">availableData</a>&gt;
            &lt;<a href="#seismogramType">seismogram</a>&gt;
            &lt;<a href="#seismogramProcessType">seismogramProcess</a>&gt;
        &lt;/localSeismogramArm&gt;
    &lt;/waveFromArm&gt;
&lt;sod&gt;

</pre></p>
       
         <xsl:apply-templates select="xsd:schema" />
      </body>
    </html>
  </xsl:template>

  <xsl:template match="xsd:schema" >
    <p>Doing a schema</p>
    <xsl:variable name="filelist" >
      <xsl:apply-templates select="xsd:include" mode="findfiles" >
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:copy-of select="$filelist"/>

    <xsl:for-each select="xsd:include" >
      <xsl:value-of select="@schemaLocation"/><br/>
    </xsl:for-each>
    <xsl:for-each select="xsd:include" >
      <p>Found an include aaa <xsl:value-of select="@schemaLocation" /></p>
      <xsl:apply-templates select="document(@schemaLocation)/xsd:schema/xsd:complexType|document(@schemaLocation)/xsd:schema/xsd:element" />
    </xsl:for-each>
    <xsl:apply-templates select="xsd:complexType|xsd:element" />
  </xsl:template>



  <xsl:template match="xsd:complexType" >
    <xsl:variable name="tag-name" select="@name"/>
    <h3 id="{$tag-name}" >
      <xsl:if test="@abstract='true'" >
        <xsl:text>Abstract </xsl:text>    
      </xsl:if>
      <xsl:value-of select="@name" />
    </h3>
    <xsl:if test="xsd:complexContent/xsd:extension">
      <p>
        <xsl:text> extends </xsl:text>
        <xsl:variable name="base" select="xsd:complexContent/xsd:extension/@base"/>
        <a href="#{$base}">
          <xsl:value-of select="$base"/>
        </a>
      </p>
    </xsl:if>
      <xsl:if test="@abstract='true'" >
    <p>
      <h4>
        <xsl:text>All known subclasses</xsl:text>
      </h4>
      <table>
      <xsl:for-each select="//xsd:complexType[xsd:complexContent/xsd:extension/@base=$tag-name]" >
        <xsl:sort select="@name" />
        <xsl:variable name="subclass" select="@name"/>
        <tr><td><a href="#{$subclass}">
          <xsl:value-of select="$subclass"/>
        </a></td>
        <td><xsl:value-of select="xsd:annotation/xsd:documentation/summary" />
        <xsl:text> </xsl:text>
        </td>
        </tr>
      </xsl:for-each>
    </table>
    </p>
  </xsl:if>
    <p>
      <xsl:apply-templates select="xsd:annotation" />
    </p>
    <p>
      <h4><xsl:text>Elements</xsl:text></h4>
      <xsl:apply-templates select="xsd:sequence|xsd:complexContent" />
    </p>
    <hr/>
  </xsl:template>

  <xsl:template match="comment()" mode="comments">
    <h4><xsl:text>Comment</xsl:text></h4>
    
    <p>
      <xsl:value-of select="."/>
    </p>
  </xsl:template>  

  <xsl:template match="xsd:complexContent" >
      <xsl:apply-templates select="xsd:extension" />
  </xsl:template>

  <xsl:template match="xsd:extension" >
    <xsl:variable  name="abstractBase">
      <xsl:value-of  select="@base" />
    </xsl:variable>
    <xsl:apply-templates select="//xsd:complexType[@name=$abstractBase]//xsd:sequence" />
    <xsl:apply-templates select="xsd:sequence" />
  </xsl:template>

  <xsl:template match="xsd:sequence" >
      <xsl:apply-templates select="xsd:element" />
  </xsl:template>

  <xsl:template match="xsd:element" >
    <xsl:choose>
      <xsl:when test="@ref">
        <xsl:variable  name="ref">
          <xsl:value-of  select="@ref" />
        </xsl:variable>
        <xsl:apply-templates select="//xsd:element[@name=$ref]" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@name" />
        <xsl:text> </xsl:text>
          <xsl:variable name="type" select="@type" />
        <a href="#{$type}">
          <xsl:value-of select="$type" />
        </a>
        <xsl:choose>
        <xsl:when test=" not( @minOccurs) and not( @maxOccurs)">
          <xsl:text> exactly once</xsl:text>
        </xsl:when>
        <xsl:when test="@minOccurs=0">
          <xsl:text> optional</xsl:text>
        </xsl:when>
        <xsl:when test="@minOccurs | @maxOccurs">
          <xsl:if test="@minOccurs" >
            <xsl:text> occures at least </xsl:text>
            <xsl:value-of select="@minOccures"/>
            <xsl:text> times </xsl:text>
          </xsl:if>
          <xsl:if test="@minOccurs" >
            <xsl:text> and at most </xsl:text>
            <xsl:value-of select="@minOccures"/>
            <xsl:text> times</xsl:text>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text> optional</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
        <br/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsd:annotation">
    <xsl:apply-templates select="xsd:documentation" />
  </xsl:template>

  <xsl:template match="xsd:documentation" >
    <h4>Summary</h4>
    <xsl:copy-of select="summary/node()" />
    <h4>Description</h4>
    <xsl:copy-of select="description/node()" />
    <h4>Example</h4>
    <code>
      <pre>
        <xsl:apply-templates select="example/node()" mode="make-literal" />
      </pre>
    </code>
  </xsl:template>

  <xsl:template match="*" mode="make-literal" >
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="name()"/>
      <xsl:if test="count(@*)">
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:for-each select="@*">
        <xsl:value-of select="name()"/>
        <xsl:text>=&quot;</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>&quot;</xsl:text>
      </xsl:for-each>
      <xsl:choose>
        <xsl:when test="node()">
          <xsl:text>&gt;</xsl:text>
          <xsl:apply-templates select="node()" mode="make-literal" />
          <xsl:text>&lt;/</xsl:text>
          <xsl:value-of select="name()"/>
          <xsl:text>&gt;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>/&gt;</xsl:text>
          
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>
  
  <xsl:template match="text()" mode="make-literal" >
    <xsl:value-of select="."/>
    
  </xsl:template>

</xsl:stylesheet>

