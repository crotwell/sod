<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
  xmlns:xsltc="http://xml.apache.org/xalan/xsltc"
  xmlns:redirect="http://xml.apache.org/xalan/redirect"
  extension-element-prefixes="redirect"
  version="1.0">
  
  <xsl:output method="html"/>

  <xsl:template match="/">
     <xsltc:output file="blob.html">
       <xsl:text>This ends up in the file 'blob.xml'</xsl:text>
     </xsltc:output>
     <redirect:write file="blob.html">
       <xsl:text>This is appended to the file 'blob.xml'</xsl:text>
     </redirect:write>

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
    &lt;<a href="#propertiesType">properties</a>&gt;
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
            &lt;<a href="#requestSubsetterType">requestSubsetter</a>&gt;
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
    <xsl:apply-templates select="xsd:schema" mode="index.html" />
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
      <xsl:apply-templates select="document(@schemaLocation)/xsd:schema/xsd:complexType" mode="abstractPage"/>
    </xsl:for-each>
    <xsl:apply-templates select="xsd:complexType" />
    <xsl:apply-templates select="xsd:complexType" mode="abstractPage" />
  </xsl:template>



  <xsl:template match="xsd:complexType" >
    <xsl:variable name="tag-name" select="@name"/>
 <redirect:write file="{$tag-name}.html">
   <html>
     <body>
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
        <a href="{$base}.html">
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
        <tr><td><a href="{$subclass}.html">
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
</body>
</html>
</redirect:write>
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
        <a href="{$type}.html">
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

  <xsl:template match="*" mode="index.html" >
    <redirect:write file="index.html">
    <html>
<head>
<title>SOD Tag Documentation</title>
</head>
<frameset cols="20%,80%">
<frameset rows="30%,70%">
  <xsl:apply-templates select="." mode="overview-frame" />
  <frame src="overview-frame.html" name="packageListFrame" />
  <xsl:apply-templates select="." mode="allclasses-frame" />
  <frame src="allclasses-frame.html" name="packageFrame" />
</frameset>
  <xsl:apply-templates select="." mode="overview-summary" />
<frame src="overview-summary.html" name="classFrame" />
</frameset>
<noframes>

<h2>Frame Alert</h2>

<p>
  This document is designed to be viewed using the frames feature. If you see this message, you are using a non-frame-capable web client.</p>
  <br/>
Link to<a HREF="overview-summary.html">Non-frame version.</a>
</noframes>
</html>
</redirect:write>
  </xsl:template>

  <xsl:template match="*" mode="allclasses-frame">
    <redirect:write file="allclasses-frame.html">

<!--NewPage-->
<html>
<head>
<!-- Generated by javadoc on Sun May 06 06:06:49 PDT 2001 -->
<title>
All Classes
</title>
<link REL ="stylesheet" TYPE="text/css" HREF="stylesheet.css" TITLE="Style"/>
</head>
<body BGCOLOR="white">
<font size="+1" CLASS="FrameHeadingFont">
<b>All Classes</b></font>
<br/>

<font CLASS="FrameItemFont">
<table BORDER="0" WIDTH="100%">

      <xsl:for-each select="//xsd:include" >
        <xsl:for-each select="document(@schemaLocation)/xsd:schema/xsd:complexType">
          <xsl:sort select="@name" />
<tr>
<td>
<a TARGET="classFrame">
   <xsl:attribute name="HREF">
   <xsl:value-of select="@name"/>
   <xsl:text>.html</xsl:text>
   </xsl:attribute>
   <xsl:value-of select="@name"/>
</a>
</td>
</tr>
        </xsl:for-each>
      </xsl:for-each>
</table>
</font>

</body>
</html>
    </redirect:write>
  </xsl:template>

  <xsl:template match="*" mode="overview-frame">
    <redirect:write file="overview-frame.html">
      <html>
        <head>

          <title>
            SOD Tag Documentation: Overview
          </title>
          <link REL ="stylesheet" TYPE="text/css" HREF="stylesheet.css" TITLE="Style" />
          </head>
          <body BGCOLOR="white">

            <table BORDER="0" WIDTH="100%">
              <tr>
                <td><font size="+1" CLASS="FrameTitleFont">
                <b><b>SOD Tag Documentation</b></b></font></td>

              </tr>
            </table>

            <table BORDER="0" WIDTH="100%">
              <tr>
                <td ><font CLASS="FrameItemFont"><a HREF="allclasses-frame.html" TARGET="packageFrame">All Tags</a></font>
                <p/>
                <font size="+1" CLASS="FrameHeadingFont">
                  Sod Types</font>
                  <br/>
                  <font CLASS="FrameItemFont">
                  <pre>
&lt;sod&gt;
   &lt;eventArm&gt;
      &lt;<a  TARGET="packageFrame" href="eventFinderType_subTypes.html">eventFinder</a>&gt; 
   or &lt;<a  TARGET="packageFrame" href="eChanFinderType_subTypes.html">eventChannelFinder</a>&gt;
      &lt;<a  TARGET="packageFrame" href="eventAttrType_subTypes.html">eventAttr</a>&gt;
      &lt;<a  TARGET="packageFrame" href="originType_subTypes.html">origin</a> &gt;
      &lt;<a  TARGET="packageFrame" href="eventProcessType_subTypes.html">eventProcess</a>&gt;
   &lt;/eventArm&gt;
   &lt;networkArm&gt;
      &lt;<a  TARGET="packageFrame" href="networkFinderType_subTypes.html">networkFinder</a>&gt;
      &lt;<a  TARGET="packageFrame" href="networkIDType_subTypes.html">networkID</a>&gt;
      &lt;<a  TARGET="packageFrame" href="networkAttrType_subTypes.html">networkAttr</a>&gt;
      &lt;<a  TARGET="packageFrame" href="stationIDType_subTypes.html">stationID</a>&gt;
      &lt;<a  TARGET="packageFrame" href="stationType_subTypes.html">station</a>&gt;
      &lt;<a  TARGET="packageFrame" href="siteIDType_subTypes.html">siteID</a>&gt;
      &lt;<a  TARGET="packageFrame" href="siteType_subTypes.html">site</a>&gt;
      &lt;<a  TARGET="packageFrame" href="channelIDType_subTypes.html">channelID</a>&gt;
      &lt;<a  TARGET="packageFrame" href="channelType_subTypes.html">channel</a>&gt;
      &lt;<a  TARGET="packageFrame" href="networkProcessType_subTypes.html">networkProcess</a>&gt;
   &lt;/networkArm&gt;
   &lt;waveFormArm&gt;
      &lt;<a  TARGET="packageFrame" href="eventStationType_subTypes.html">eventStation</a>&gt;
      &lt;localSeismogramArm&gt;
         &lt;<a  TARGET="packageFrame" href="eventChannelType_subTypes.html">eventChannel</a>&gt;
         &lt;<a  TARGET="packageFrame" href="requestGeneratorType_subTypes.html">requestGenerator</a>&gt;
         &lt;<a  TARGET="packageFrame" href="requestSubsetterType_subTypes.html">requestSubsetter</a>&gt;
         &lt;<a  TARGET="packageFrame" href="dataCenterType_subTypes.html">dataCenter</a>&gt;
         &lt;<a  TARGET="packageFrame" href="availableDataType_subTypes.html">availableData</a>&gt;
         &lt;<a  TARGET="packageFrame" href="seismogramType_subTypes.html">seismogram</a>&gt;
         &lt;<a  TARGET="packageFrame" href="seismogramProcessType_subTypes.html">seismogramProcess</a>&gt;
      &lt;/localSeismogramArm&gt;
   &lt;/waveFromArm&gt;
&lt;sod&gt;              
                </pre>                  
              </font>
            </td>
            </tr>
          </table>
          
          <p/>
          
        </body>
      </html>
    </redirect:write>
  </xsl:template>
    

  <xsl:template match="*" mode="overview-summary">
    <redirect:write file="overview-summary.html">

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
      &lt;<a href="eventFinderType.html">eventFinder</a>&gt; or &lt;<a href="eventChannelFinderType.html">eventChannelFinder</a>&gt;
      &lt;<a href="eventAttrType.html">eventAttr</a>&gt;
      &lt;<a href="originType.html">origin</a> &gt;
      &lt;<a href="eventProcessType.html">eventProcess</a>&gt;
   &lt;/eventArm&gt;
   &lt;networkArm&gt;
      &lt;<a href="networkFinderType.html">networkFinder</a>&gt;
      &lt;<a href="networkIDType.html">networkID</a>&gt;
      &lt;<a href="networkAttrType.html">networkAttr</a>&gt;
      &lt;<a href="stationIDType.html">stationID</a>&gt;
      &lt;<a href="stationType.html">station</a>&gt;
      &lt;<a href="siteIDType.html">siteID</a>&gt;
      &lt;<a href="siteType.html">site</a>&gt;
      &lt;<a href="channelIDType.html">channelID</a>&gt;
      &lt;<a href="channelType.html">channel</a>&gt;
      &lt;<a href="networkProcessType.html">networkProcess</a>&gt;
   &lt;/networkArm&gt;
   &lt;waveFormArm&gt;
      &lt;<a href="eventStationType.html">eventStation</a>&gt;
      &lt;localSeismogramArm&gt;
         &lt;<a href="eventChannelType.html">eventChannel</a>&gt;
         &lt;<a href="requestGeneratorType.html">requestGenerator</a>&gt;
         &lt;<a href="requestSubsetterType.html">requestSubsetter</a>&gt;
         &lt;<a href="dataCenterType.html">dataCenter</a>&gt;
         &lt;<a href="availableDataType.html">availableData</a>&gt;
         &lt;<a href="seismogramType.html">seismogram</a>&gt;
         &lt;<a href="seismogramProcessType.html">seismogramProcess</a>&gt;
      &lt;/localSeismogramArm&gt;
   &lt;/waveFromArm&gt;
&lt;sod&gt;              
            </pre>
          </p>           
        </body>
      </html>
    </redirect:write>
  </xsl:template>
  
  <xsl:template match="xsd:complexType" mode="abstractPage">
    <xsl:variable name="tag-name" select="@name"/>
    <xsl:if test="@abstract='true'" >
      <redirect:write file="{@name}_subTypes.html" >
        <html>
<title>
<xsl:value-of select="$tag-name"/>
</title>
          <body>
            <h3><xsl:value-of select="$tag-name" /></h3><br/>
            <xsl:for-each select="//xsd:complexType[xsd:complexContent/xsd:extension/@base=$tag-name]" >
              <xsl:sort select="@name" />
              <xsl:variable name="subclass" select="@name"/>
              <a TARGET="classFrame">
                <xsl:attribute name="HREF">
                  <xsl:value-of select="@name"/><xsl:text>.html</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="@name"/><br/>
              </a>
              
            </xsl:for-each>
          </body>
        </html>
      </redirect:write>
    </xsl:if>
    <xsl:if test="@name='eventFinderType'">
      <redirect:write file="{$tag-name}_subTypes.html" >
        <html>
          <body>
              <a TARGET="classFrame">
                <xsl:attribute name="HREF">
                  <xsl:value-of select="@name"/><xsl:text>.html</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="@name"/><br/>
              </a>
              
          </body>
        </html>
      </redirect:write>
    </xsl:if>
    <xsl:if test="@name='eventChannelFinderType'">
      <redirect:write file="eChanFinderType_subTypes.html" >
        <html>
          <body>
              <a TARGET="classFrame">
                <xsl:attribute name="HREF">
                  <xsl:value-of select="@name"/><xsl:text>.html</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="@name"/><br/>
              </a>
              
          </body>
        </html>
      </redirect:write>
    </xsl:if>
    <xsl:if test="@name='networkFinderType'">
      <redirect:write file="{$tag-name}_subTypes.html" >
        <html>
          <body>
              <a TARGET="classFrame">
                <xsl:attribute name="HREF">
                  <xsl:value-of select="@name"/><xsl:text>.html</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="@name"/><br/>
              </a>
              
          </body>
        </html>
      </redirect:write>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>

