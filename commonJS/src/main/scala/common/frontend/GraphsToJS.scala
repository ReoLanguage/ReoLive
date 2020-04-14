package common.frontend

import hub.DSL
import preo.backend._

import scala.collection.Map
import scala.reflect.ClassTag


//todo: add rectangle colision colision
object GraphsToJS {
  def apply(graph: Circuit): String = {
    val mark = "gr"//graph.hashCode()
    generateJS(getNodes(graph,mark), getLinks(graph,mark))
  }

  def toVirtuosoJs(graph:Circuit):String = {
    val mark = "gr" //graph.hashCode()
    generateJS(getNodes(graph,mark,virtuoso=true), getLinks(graph,mark))
  }

  private def generateJS(nodes: String, edges: String): String = {
    // println(s"""var graph = {"nodescircuit": $nodes, "linkscircuit": $edges};""")
    s"""
        var svg = d3.select("#circuit");
        var vbox = svg.attr('viewBox').split(" ")
        var width  = vbox[2]; //svg.attr("width");
        var height = vbox[3]; //svg.attr("height");
        var radius = 7.75;
        var rectangle_width  = 40; // of component boxes
        var rectangle_height = 20; // of component boxes
        var hubSize = 22;

        var graph = {"nodescircuit": $nodes, "linkscircuit": $edges};

        var simulation = d3.forceSimulation(graph.nodescircuit)
          .force('charge', d3.forceManyBody().strength(-700))
          .force('center', d3.forceCenter(width / 2, height / 2))
          .force('y', d3.forceY().y(function(d) { return 0;}))
          .force('x', d3.forceX().x(function(d) {
            if (d.group == "rd" || d.group == "snk"){
              return width/2;
            }
            if (d.group == "wr" || d.group == "src"){
              return -width/2;
            }
            return 0;
          }))
          .force('collision', d3.forceCollide().radius(function(d) {
                       return d.radius}))
          .force('link', d3.forceLink().links(graph.linkscircuit).id(function(d) { return d.id; }).distance(45))
          //.force("forcepos", forcepos)
          .on('tick', ticked);

        init(graph.nodescircuit, graph.linkscircuit);

        function init(nodes, links){
            //add nodes (nodes "circle" with group in {1,2,3})
            var node = d3.select(".nodescircuit").selectAll("circle")
                .data(nodes.filter(function(d){
                  //return d.group >"0" && d.group < "4"
                  return (d.group == "src" || d.group == "mix" || d.group == "xrouter" | d.group == "snk");
                  }));
            node.enter()
                .append("circle")
                .merge(node)
                .attr("r", radius - 0.75)
                .attr("id", function (d) {return d.id;})
                .on("mouseenter", function(d) {
                  console.log("node id:"+d.id + "; ports: " +d.ports );})
                .call(d3.drag()
                  .on("start", dragstarted)
                  .on("drag", dragged)
                  .on("end", dragended))
                .style("stroke-opacity" , "1")
                .style("stroke-width", function(d){
                  if(d.group == "xrouter") {return "1px";} else {return "2px";}
                })
                .style("stroke", function(d){
                   if(d.group == "src" || d.group == "snk" || d.group == "xrouter"){
                     return "black";
                   }
                   else{
                     return "white";
                   }
                })
                .style("fill", function(d){
                  if(d.group == "src" || d.group == "snk"){
                    return "white";
                  }
//                  else if (d.group == "xrouter"){
//                    return "url(#xorpattern)";
//                    return "none";
//                  }
                  else{
                    return "black";
                  }
                })
                .style("fill-opacity", function(d){
                  if (d.group == "xrouter"){
                    return "0.2";
                  }
                  else {
                    return "";
                  }})
//                .style("background-color", function(d){
//                  if (d.group == "xrouter"){
//                    return "gray";
//                  }
//                  else {
//                    return "black";
//                  }})
                .append("text").text("X");
            node.exit().remove();

            // add components (nodes "rect" with group in {0,4})
            var rects = d3.select(".nodescircuit").selectAll(".component")
                .data(nodes.filter(function(d){
                  return d.group == "wr" || d.group == "rd"
                }));

            var rect = rects.enter();
            var rg = rect
                 .append("g")
                 .attr("class","component");
            rg.attr("id", function (d) {return d.id;});
            rg   .append("rect")
//                .merge(rects)
//                 .attr("id", function (d) {return d.id;})
//                 .attr("class","component") // test
                 .attr('width', rectangle_width)
                 .attr('height', rectangle_height)
                 .attr("y","-10px")
                .on("mouseenter", function(d) {
                  console.log("node id:"+d.id + "; ports: " +d.ports );})
                 .call(d3.drag()
                   .on("start", dragstarted)
                   .on("drag", dragged)
                   .on("end", dragended))
                 .style("stroke", "black")
                 .attr("fill", "#ffd896");
            rg
                .append("text")
                .attr("transform","translate(5,4)");
                //.text("TEXT HERE!");

            rects.exit().remove();

            // add boxes (nodes "rect" with group 5)
            var boxes = d3.select(".nodescircuit").selectAll(".box")
                .data(nodes.filter(function(d){
                  return d.group == "box"
                }));

            var box = boxes.enter();
            var rg = box
                 .append("g")
                 .attr("class","box");
            rg.attr("id", function (d) {return d.id;});
            rg   .append("rect")
//                 .attr("class","box")
//                 .attr("id", function (d) {return d.id;})
                 .attr('width', function (d) {return ((d.name.length*8.3) + 10);} )
                 .attr('height', rectangle_height)
                 .attr("y","-10px")
                  .on("mouseenter", function(d) {
                      console.log("node id:"+d.id + "; ports: " +d.ports );})
                 .call(d3.drag()
                   .on("start", dragstarted)
                   .on("drag", dragged)
                   .on("end", dragended))
                 .style("stroke", "black")
                 .attr("fill", "#d2e2ff");
            rg
                .append("text")
                .attr("transform","translate(5,4)")
                .text( function (d) {return d.name;} );

            boxes.exit().remove();

            // add Virtuoso Hubs
            var hubs = d3.select(".nodescircuit").selectAll(".hub")
              .data(nodes.filter(function(d) {
//                 return (d.group >=6 && d.group <= 12 );
                return (d.group == "mrg" || d.group == "dupl" || d.group == "xor" || d.group == "semaphore" ||
                        d.group == "fifo" || d.group =="resource" || d.group == "event" || d.group == "blackboard"
                        || d.group == "dataEvent" || d.group == "drain" || d.group == "port"
                        || d.group == "eventFull" || d.group == "dataEventFull" || d.group == "fifoFull"
                        || d.group == "blackboardFull" || d.group == "await" || d.group == "timeout");
              }));
            var hub = hubs.enter();
            var rg = hub.append("g").attr("class","hub");
                rg.attr("id", function(d) {return d.id;});
                rg.append("image")
                  .attr("width",hubSize)
                  .attr("height",hubSize)
                  .attr("xlink:href", function(d) {return "svg/"+d.group+".svg";})
                  .on("mouseenter", function(d) {
                    console.log("node id:"+d.id + "; ports: " +d.ports );})
                  .call(d3.drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    .on("end", dragended));
            hubs.exit().remove();

             //add links
             var link = d3.select(".linkscircuit").selectAll("polyline")
                .data(links);
             link.enter()
                .append("polyline")
                .style("stroke", "black")
                .merge(link)
                .attr('marker-end', function(d){
                  return 'url(#' + d.end + ')'
                })
                .attr('marker-mid', function(d) {
                  if (d.type === "fifo"){
                     return 'url(#boxmarkercircuit)'
                  } else if (d.type === "fifofull"){
                     return 'url(#boxfullmarkercircuit)'
                  } else if (d.type === "timer") {
                     return 'url(#timermarkercircuit)'
                  } else if (d.type === "rid") {
                     return 'url(#wavemarkercircuit)'
                  } else {
                   return ("");
                }})
                .attr('marker-start', function(d){
                  return 'url(#' + d.start + ')'
                })
                .style('stroke-dasharray', function(d){
                  if(d.type === "lossy"){
                    return ("3, 3");
                  }
                  else {
                    return ("1, 0");
                  }}) ;


            link.append("title")
                .text(function (d) {return d.type;});
            link.exit().remove();

            //add labels to graph
            var edgepaths = svg.select(".pathscircuit").selectAll(".edgepath")
                .data(links);
            edgepaths.enter()
                .append('path')
                .attr('class', 'edgepath')
                .attr('fill-opacity', 0)
                .attr('stroke-opacity', 0)
                .attr('id', function (d, i) {return 'edgepathcircuit' + i})
                .style("pointer-events", "none");
            edgepaths.exit().remove();

            var edgelabels = svg.select(".labelscircuit").selectAll(".edgelabel")
                .data(links);
            edgelabels.enter()
                .append('text')
                .style("pointer-events", "none")
                .attr('class', 'edgelabel')
                .attr('id', function (d, i) {return 'edgelabelcircuit' + i})
                .attr('font-size', 14)
                .attr('fill', 'black');
            edgelabels.exit().remove();

            d3.select(".labelscircuit").selectAll("textPath").remove();

            var textpath = d3.select(".labelscircuit").selectAll(".edgelabel")
                .append('textPath')
                .attr('xlink:href', function (d, i) {return '#edgepathcircuit' + i})
                .style("text-anchor", "middle")
                .style("pointer-events", "none")
                .attr("startOffset", "50%")
                .text(function (d) {
                  if(d.type === "drain" || d.type === "lossy" || d.type === "merger" ||
                     d.type === "sync" || d.type === "fifo" || d.type === "rid" || d.type
                     === "fifofull" || d.type === "timer" ||
                     d.type.startsWith("NW ") || d.type.startsWith("W ") ||"""+raw"""/^\d/.test(d.type)) {""" +s"""
                      return "";
                  }else {
                    return d.type;
                  }
                });
                textpath.append("tspan")
                    .attr("class", "sync-type")
                    .style("fill","#3B01E9")
                    .style("font-size", "8px")
                    .text(function (d) {
                      if (d.type.startsWith("NW ") || d.type.startsWith("W ") ||"""+raw""" /^\d/.test(d.type)) {""" +s"""
                        return d.type.split(" ")[0]+" "
                      } else return  ""
                    });
                textpath.append("tspan")
                    .attr("class", "port-name")
                    .style("font-size", "8px")
                    .text(function (d) {
                      if (d.type.startsWith("NW ") || d.type.startsWith("W ") ||"""+raw""" /^\d/.test(d.type)) {""" +s"""
                        return d.type.split(" ")[1];
                      } else return  ""
                    });

            var timerTextPath = d3.select(".labelscircuit").selectAll(".edgelabel")
                .append('textPath')
                .attr('xlink:href', function (d, i) {return '#edgepathcircuit' + i})
                .style("text-anchor", "middle")
                .style("pointer-events", "none")
                .style("font-size","8px")
                .attr("startOffset", "50%")
                .append("tspan")
                .style("fill","#00B248")
                .attr("dy","0.3em")
                .text(function (d) {
                  if (d.type === "timer") {
                    return d.to
                  } else return "";
                });

        }

        function ticked() {
            var half_hight_rect = rectangle_height/2;
            // MOVE NODES
            var node = d3.select(".nodescircuit").selectAll("circle")
                .attr('cx', function(d) {return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
                .attr('cy', function(d) {return d.y = Math.max(radius, Math.min(height - radius, d.y)); });

            // MOVE BOXES
            var box = d3.select(".nodescircuit").selectAll(".box")
                .attr('cx', function(d) {
                    var half_width_rect = ((d.name.length*8.3) +10)/2;
                    return d.x = Math.max(half_width_rect , Math.min(width  - half_width_rect, d.x));})
                .attr('cy', function(d) {
                    return d.y = Math.max(half_hight_rect, Math.min(height - half_hight_rect, d.y));});
                // move box rectangle
                box.selectAll("rect").attr("transform",function(d) {
                    var half_width_rect = ((d.name.length*8.3) +10)/2;
                    return "translate("+(d.x-half_width_rect)+","+(d.y)+")"; } );
                // move box text
                box.selectAll("text").attr("transform",function(d) {
                    var half_width_rect = ((d.name.length*8.3) +10)/2;
                    return "translate("+(5+d.x-half_width_rect)+","+(d.y+4)+")"; } );

            // MOVE HUBS
            var hubs = d3.select(".nodescircuit").selectAll(".hub")
                  .attr('cx', function(d) { return d.x = Math.max(hubSize/2, Math.min(width  - hubSize/2 , d.x)); } )
                  .attr('cy', function(d) { return d.y = Math.max(hubSize/2, Math.min(height - hubSize/2, d.y)); });
                // move hub figure
                hubs.selectAll("image").attr("transform",function(d) {
                      return "translate("+ (d.x - (hubSize/2)) +","+(d.y-(hubSize/2))+")"; } );

            // MOVE COMPONENTS
            var rect = d3.select(".nodescircuit").selectAll(".component")
                .attr('cx', function(d) { return d.x = Math.max(0, Math.min(width  - rectangle_width , d.x)); } )
                .attr('cy', function(d) { return d.y = Math.max(10, Math.min(height - half_hight_rect, d.y)); });
                // move component rectangle
                rect.selectAll("rect")
                  .attr("transform",function(d) {
                    return "translate("+Math.max(0,Math.min(width-rectangle_width,d.x))+","+Math.max(half_hight_rect,Math.min(height - half_hight_rect, d.y))+")"; } );

            // MODIFY LINES AND TEXT
            var link = d3.select(".linkscircuit").selectAll("polyline")
                .attr("points", function(d) {
                    return d.source.x + "," + d.source.y + " " +
                    (d.source.x + d.target.x)/2 + "," + (d.source.y + d.target.y)/2 + " " +
                    d.target.x + "," + d.target.y; });
//                .attr("x1", function(d) { return d.source.x; })
//                .attr("y1", function(d) { return d.source.y; })
//                .attr("x2", function(d) { return d.target.x; })
//                .attr("y2", function(d) { return d.target.y; });
            d3.select(".pathscircuit").selectAll(".edgepath").attr('d', function (d) {
                return 'M ' + d.source.x + ' ' + d.source.y + ' L ' + d.target.x + ' ' + d.target.y;
            });
            d3.select(".labelscircuit").selectAll(".edgelabel").attr('transform', function (d) {
                if (d.target.x < d.source.x) {
                    var bbox = this.getBBox();
                    rx = bbox.x + bbox.width / 2;
                    ry = bbox.y + bbox.height / 2;
                    return 'rotate(180 ' + rx + ' ' + ry + ')';
                }
                else {
                    return 'rotate(0)';
                }
            });
        }


        function dragstarted(d) {
          if (!d3.event.active) simulation.alphaTarget(0.3).restart();
          d.fx = d.x;
          d.fy = d.y;
        }
        function dragged(d) {
          d.fx = d3.event.x;
          d.fy = d3.event.y;
        }
        function dragended(d) {
          if (!d3.event.active) simulation.alphaTarget(0);
//          if (d.group == "snk" || d.group == "src" || d.group == "box"){
            d.fx = null;
            d.fy = null;
//          }
        }
      """
  }


  private def getNodes(graph: Circuit, mark:String, virtuoso:Boolean = false): String =
    graph.nodes.map(processNode(_, mark, virtuoso)).mkString("[", ",", "]")


  private def getLinks(graph: Circuit, mark:String): String =
    graph.edges.map(processEdge(_,mark)).mkString("[",",","]")


  private def processNode(node:ReoNode,mark:String,virtuoso:Boolean = false):String = node match {
    case ReoNode(id, name, nodeType, extra,ports) => {
      val nodeGroup = typeToGroup(nodeType, extra,virtuoso);
//      val ports:Set[Int] = Set()
      s"""{"id": "${mark}_$id", "group": "$nodeGroup", "name": "${name.getOrElse("")}", "ports" : ${ports.map(p => s""""${p.toString}"""").mkString("[",",","]")} }"""
    }
  }

  /**
    * Select the right group:
    *  - 0: source component
    *  - 1: source node
    *  - 2: mixed node
    *  - 3: sink node
    *  - 4: sink component
    *  - 5: box (container)
    *  - 6: DataEvent
    *  - 7: Event
    *  - 8: BlackBoard
    *  - 9: Fifo
    *  - 10: Port
    *  - 11: Resource
    *  - 12: Semaphore
    * @param nodeType if it is source, sink, or mixed type
    * @param extra optional field that may contain "component"
    * @return
    */
  private def typeToGroup(nodeType: NodeType, extra: Set[Any],virtuoso:Boolean=false):String = nodeType match{
    case Source => if (extra.contains("component")) "wr"  else "src"
    case Sink   => if (extra.contains("component")) "rd"  else "snk"
    case Mixed  =>
      if (extra.contains("box"))
        "box"
      else if (virtuoso) {
        //(extra - "mrg").headOption.getOrElse("xor").toString
        val hub = extra.filter(e=>e.isInstanceOf[String]).asInstanceOf[Set[String]].find(e=> (DSL.hubs++DSL.primitiveConnectors-"mrg").contains(e))
        if (hub.isDefined) hub.get else "xor"
      }
      else if (extra.contains("xor"))
        "xrouter"
      else
        "mix"
      // todo: probably "xor" should be "port", now just to show a P instead of a mixed node
  }

  private def processEdge(channel: ReoChannel,mark:String): String = channel match{
    case ReoChannel(src,trg, srcType, trgType, name, style) => {
      var start = arrowToString(srcType);
      var end = arrowToString(trgType);
      var to = if (name == "timer") getTimerInfo(channel) else ""
        s"""{"source": "${mark}_$src", "target": "${mark}_$trg", "type":"$name", "start":"start${start}circuit", "end": "end${end}circuit", "to": "${to}"}"""
    }
  }

  private def getTimerInfo(channel: ReoChannel):Int = {
//    var info = channel.extra.iterator.filter(e => e.isInstanceOf[(String,Int)]).map(e => e.asInstanceOf[(String,Int)])
//    info.toMap.getOrElse("to",0)
    var extraInfo = channel.extra.iterator.filter(e => e.isInstanceOf[String]).map(e => e.asInstanceOf[String])
    extraInfo.find(e => e.startsWith("to:")) match {
      case Some(s) => s.drop(3).toInt
      case _ => 0
    }
  }

  private def arrowToString(endType: EndType): String = endType match{
    case ArrowIn => "arrowin"
    case ArrowOut => "arrowout"
    case _ => ""
  }
}



