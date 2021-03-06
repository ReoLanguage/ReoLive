package common.frontend

import dsl.analysis.semantics.{SBAutomata, SBState, SBTrans, UpdTrans}
import hub.{CCons, HubAutomata}
import preo.backend.Network.Mirrors
import ifta.analyse.Simplify
import ifta.{CTrue, ClockCons}
import ifta.backend.{IftaAutomata, Show}
import preo.backend._

import scala.util.Try


//todo: add rectangle colision colision
object AutomataToJS {

  //todo: Maybe create a special abstract automata that has time so that ifta and hub can inherit from it
  def apply[A<:Automata](aut: A, mirrors: Mirrors, boxName:String, portNames:Boolean=false): String = aut match {
    case a: IftaAutomata => generateJS(getInvNodes(a), getLinks(aut, boxName, portNames, mirrors), boxName)
    case a:HubAutomata => generateJS(getInvNodes(a), getLinks(aut, boxName, portNames, mirrors), boxName)
    case _ => generateJS(getNodes(aut), getLinks(aut, boxName, portNames, mirrors), boxName)
  }

//  def toJs[A<:Automata](aut: A, boxName:String, portNames:Boolean=false, mirrors: Mirrors=new Mirrors): String = aut match {
//    case a:IftaAutomata => generateJS(getIftaNodes(a) , getLinks(aut,boxName,portNames,mirrors), boxName)
//    case _              => generateJS(getNodes(aut)   , getLinks(aut,boxName,portNames,mirrors), boxName)
//  }


  def apply(aut:SBAutomata, boxName:String):String =
    generateJS(getNodes(aut),getLinks(aut,boxName),boxName)


  /*todo: refactor in different methods or classes to avoid booolean virtuoso, or pass automata for
   * better customization for each type of automata, reo, ifta, hub,sb*/
  private def generateJS(nodes: String, edges: String, name:String): String = {
    // println(nodes)
    // println(edges)
    s"""
        buildAut();
        function buildAut() {
          var svgAut = d3.select("#${name}");
          var vboxAut = svgAut.attr('viewBox').split(" ")
          var widthAut = vboxAut[2]; //svgAut.attr("widthAut");
          var heightAut = vboxAut[3];  //svgAut.attr("heightAut");
          var radiusAut = 4.75;

          var graphAut = {"nodesautomata": ${nodes}, "linksautomata": ${edges}};

          var simulationAut = d3.forceSimulation(graphAut.nodesautomata)
            .force('charge', d3.forceManyBody().strength(-200))
            .force('center', d3.forceCenter(widthAut / 2, heightAut / 2))
            .force('y', d3.forceY().y(function(d) { return 0;}))
            .force('x', d3.forceX().x(function(d) {
//              if (d.group == "1"){
//                return widthAut;
//              }
//              if (d.group == "0"){
//                return -widthAut;
//              }
              return 0;
            }))
            .force('collision', d3.forceCollide().radius(function(d) {
                         return d.radius}))
            .force("link", d3.forceLink().links(graphAut.linksautomata).id(function(d) { return d.id; }).distance(5))
            //.force("forcepos", forcepos)
            .on('tick', tickedAut);

          initAut(graphAut.nodesautomata, graphAut.linksautomata);

          function initAut(nodesAut, linksAut){
              //add nodes (nodes "circle" with group 0..2)
              var node = d3.select(".nodes${name}")
                  .selectAll("circle")
                  .data(nodesAut);
//                  .enter().append("g");
              var nd = node.enter().append("g")
//                  .append("g").append("circle")
//              var nd = node.append("circle")
                  nd.append("circle")
                  .merge(node)
                  .attr("r", function(d){
                    if(d.group == 0 || d.group == 1){
                      return radiusAut + 0.75;
                    }
                    else{
                      return 0;
                    }
                  })
                  .attr("id", function (d) {return d.id;})
                  .call(d3.drag()
                  .on("start", dragstartedAut)
                  .on("drag", draggedAut)
                  .on("end", dragendedAut))
                  .style("stroke-opacity" , "1")
                  .style("stroke-width", function(d){
                    if(d.group == 0 || d.group == 1) {
                      return "1px";
                    } else {
                      return "0px";
                    }
                  })
                  .style("stroke", "black")
                  .style("fill", function(d){
                    if(d.group == 0) {
                      return "white";
                    }
                    else if (d.group==1) {
                      return "black";
                    }
                    else{
                      return "green";
                    }
                });

                // add invariants to timed automata nodes (ifta,hub) or state information to arx automata
                var nodelabel = nd.append("text")
                  .style("font-size","6px")
                  .style("fill","#A742A8")
                  .attr('dy', 9)
                  .attr('dx', 6)
                  .on("mouseenter", function(d) {
                    d3.select(this).style("font-size","14px");
                  })
                  .on("mouseleave", function(d) {
                    d3.select(this).style("font-size","6px");
                  })
                  .text(function(d) {
                    if (d.group == 0 || d.group == 1)
                      return d.inv;
                    else
                      return "";
                    });

              node.exit().remove();


               //add links
               var link = d3.select(".links${name}")
                  .selectAll("polyline")
                  .data(linksAut);
               link.enter().append("polyline")
                  .style("stroke", "black")
                  .merge(link)
                  .attr('marker-end', function(d){
                    return 'url(#' + d.end + ')'
                  })
                  .attr('marker-start', function(d){
                    return 'url(#' + d.start + ')'
                  })
                  .style('stroke-dasharray',"1, 0");


              link.append("title")
                  .text(function (d) {return d.type;});
              link.exit().remove();

              //add labels to graphAut
              var edgepaths = svgAut.select(".paths${name}")
                  .selectAll(".edgepath")
                  .data(linksAut);
              edgepaths.enter()
                  .append('path')
                  .attr('class', 'edgepath')
                  .attr('fill-opacity', 0)
                  .attr('stroke-opacity', 0)
                  .attr('id', function (d, i) {return 'edge${name}path' + i})
                  .style("pointer-events", "none");
              edgepaths.exit().remove();

              var edgelabels = svgAut.select(".labels${name}")       // for all labels in data
                  .selectAll(".edgelabel")
                  .data(linksAut);
              edgelabels.enter()
                  .append('text')
//                  .style("pointer-events", "none")
                  .attr('class', 'edgelabel')
                  .attr('id', function (d, i) {return 'edge${name}label' + i})
                  .attr('font-size', 10)
                  .attr('fill', 'black');
              edgelabels.exit().remove();

              d3.select(".labels${name}")
                  .selectAll("textPath").remove();

              if (${name == "automata"}) {
              var textpath = d3.select(".labels${name}")
                  .selectAll(".edgelabel")
                  .append('textPath')
                  .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                  .style("text-anchor", "middle")
                  .style("fill","#3B01E9")
                  //.style("pointer-events", "none")
                  .attr("startOffset", "50%")
                  .on("mouseenter", function(d) {
                    //console.log("me: "+d.type);
                    d3.select(this).style("font-size","14px");
                    var ports = d.type.split("~");
                    ports.shift();
                    ports.forEach(function(el) {
                      var p = document.getElementById("gr_"+el);
                      //console.log("port "+el);
                      if (p!=null && p.style.fill!="#00aaff") {
                        p.style.backgroundColor = p.style.fill;
                        p.style.fill = "#00aaff";
                        p.style.fontWeight = "bold";
                      }
                    });
                  })

                  .on("mouseleave", function(d) {
                    d3.select(this).style("font-size", "10px");
                    var ports = d.type.split("~");
                    ports.shift();
                    ports.forEach(function(el) {
                      var p = document.getElementById("gr_"+el);
                      //console.log("port "+el);
                      if (p!=null) {
                        if (p.style.backgroundColor == "")
                          {p.style.fill = "black";}
                        else
                          {p.style.fill = p.style.backgroundColor;}
                        p.style.fontWeight = "normal";                      }
                    });
                  })

                  .text(function (d) {
                    return d.type.split("~")[0];
                  });
            } else if (${name == "virtuosoAutomata"}) {
              var textpath = d3.select(".labels${name}")
                    .selectAll(".edgelabel")
                    .append('textPath')
                    .call(d3.drag()
                    .on("start", dragstartedAut)
                    .on("drag", draggedAut)
                    .on("end", dragendedAut))
                    .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                    .style("text-anchor", "middle")
                    // .style("pointer-events", "none")
                    .attr("startOffset", "50%")
                    .on("mouseenter", function(d) {
                      d3.select(this).style("font-size","14px");
                        var ports = d.type.split("~");
                        ports.shift();
                        ports.forEach(function(el) {
                          var p = document.getElementById("gr_"+el);
                          //console.log("port "+el);
                          if (p!=null && p.style.fill!="#00aaff") {
                            p.style.backgroundColor = p.style.fill;
                            p.style.fill = "#00aaff";
                            p.style.fontWeight = "bold";
                          }
                        });
                      })
                    .on("mouseleave", function(d) {
                      d3.select(this).style("font-size", "10px");
                        var ports = d.type.split("~");
                        ports.shift();
                        ports.forEach(function(el) {
                          var p = document.getElementById("gr_"+el);
                          //console.log("port "+el);
                          if (p!=null) {
                            if (p.style.backgroundColor == "")
                              {p.style.fill = "black";}
                            else
                              {p.style.fill = p.style.backgroundColor;}
                            p.style.fontWeight = "normal";
                           }
                        });
                      });
//                    .on("mouseenter", function(d) {
//                      d3.select(this).style("font-size","14px");})
//                    .on("mouseleave", function(d) {
//                      d3.select(this).style("font-size", "10px");});
                  textpath.append("tspan")
                    .attr("class", "guards")
                    .style("fill","#008900")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      return (g != "" ) ?  "〈" + g + "〉" : "";
                    });
                  textpath.append("tspan")
                    .attr("class", "ccHub")
                    .style("fill","#A1A928")
                    .text(function (d) {
                      var g = d.type.split("~")[1] ;
                      return (g != "" ) ?  g : "";
                    });
                  textpath.append("tspan")
                    .attr("class", "acts")
                    .style("fill","#3B01E9")
                    .text(function (d) {
                      var g = d.type.split("~")[1] ;
                      var a = d.type.split("~")[2] ;
                      var acts = (a !== undefined) ? a : ""
                      return (g != "" && acts!= "")? " " + acts : acts;
                    }) ;
                  textpath.append("tspan")
                    .attr("class", "updates")
                    .style("fill","#0F024F")
                    .text(function (d) {
                      var u = d.type.split("~")[3] ;
                      return (u != "" && u!== undefined) ? " " + u : "";
                    });
                  textpath.append("tspan")
                    .attr("class", "cresetsHub")
                    .style("fill","#972F65")
                    .text(function (d) {
                      var r = d.type.split("~")[4] ;
                      return (r != "" && r!== undefined) ? " " + r : "";
                    });
            } else if (${name == "sbAutomata"}) {
                  var textpath = d3.select(".labels${name}")
                    .selectAll(".edgelabel")
                    .append('textPath')
                    .call(d3.drag()
                      .on("start", dragstartedAut)
                      .on("drag", draggedAut)
                      .on("end", dragendedAut))
                    .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                    .style("text-anchor", "middle")
                    // .style("pointer-events", "none")
                    .attr("startOffset", "50%")
                    .on("mouseenter", function(d) {
                      d3.select(this).style("font-size","14px");
                        var ports = d.type.split("~");
                        ports.shift();
                        ports.forEach(function(el) {
                          var p = document.getElementById("gr_"+el);
                          //console.log("port "+el);
                          if (p!=null && p.style.fill!="#00aaff") {
                            p.style.backgroundColor = p.style.fill;
                            p.style.fill = "#00aaff";
                            p.style.fontWeight = "bold";
                          }
                        });
                      })
                    .on("mouseleave", function(d) {
                      d3.select(this).style("font-size", "10px");
                        var ports = d.type.split("~");
                        ports.shift();
                        ports.forEach(function(el) {
                          var p = document.getElementById("gr_"+el);
                          //console.log("port "+el);
                          if (p!=null) {
                            if (p.style.backgroundColor == "")
                              {p.style.fill = "black";}
                            else
                              {p.style.fill = p.style.backgroundColor;}
                            p.style.fontWeight = "normal";
                           }
                        });
                      });
                  textpath.append("tspan")
                    .attr("class", "guards")
                    .style("fill","#008900")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      return g;
                    });
                  textpath.append("tspan")
                    .attr("class", "acts")
                    .style("fill","#3B01E9")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      var a = d.type.split("~")[1] ;
                      var acts = (a !== undefined) ? a : ""
                      return (g != "" && acts!= "")? " " + acts : acts;
                    }) ;
                  textpath.append("tspan")
                    .attr("class", "updates")
                    .style("fill","#0F024F")
                    .text(function (d) {
                      var u = d.type.split("~")[2] ;
                      return (u != "" && u!== undefined) ? " " + u : "";
                    });
            } else { //iftaAutomata
                var textpath = d3.select(".labels${name}")
                    .selectAll(".edgelabel")
                    .append('textPath')
                    .call(d3.drag()
                    .on("start", dragstartedAut)
                    .on("drag", draggedAut)
                    .on("end", dragendedAut))
                    .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                    .style("text-anchor", "middle")
                    // .style("pointer-events", "none")
                    .style("font-size", "10px")
                    .attr("startOffset", "50%")
                    .on("mouseenter", function(d) {
                      d3.select(this).style("font-size","14px");
                        var ports = d.type.split("~");
                        ports.shift();
                        ports.forEach(function(el) {
                          var p = document.getElementById("gr_"+el);
                          //console.log("port "+el);
                          if (p!=null && p.style.fill!="#00aaff") {
                            p.style.backgroundColor = p.style.fill;
                            p.style.fill = "#00aaff";
                            p.style.fontWeight = "bold";
                          }
                        });
                      })
                    .on("mouseleave", function(d) {
                      d3.select(this).style("font-size", "10px");
                        var ports = d.type.split("~");
                        ports.shift();
                        ports.forEach(function(el) {
                          var p = document.getElementById("gr_"+el);
                          //console.log("port "+el);
                          if (p!=null) {
                            if (p.style.backgroundColor == "")
                              {p.style.fill = "black";}
                            else
                              {p.style.fill = p.style.backgroundColor;}
                            p.style.fontWeight = "normal";
                           }
                        });
                      });
//                  textpath.append("tspan")
//                    .attr("class", "cc")
//                    .style("fill","#00B248")
//                    .text(function (d) {
//                      var g = d.type.split("~")[0] ;
//                      return (g != "" ) ?  g : "";
//                    });
                  textpath.append("tspan")
                    .attr("class", "iftaActs")
                    .style("fill","#3B01E9")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      var a = d.type.split("~")[1] ;
                      var acts = (a !== undefined) ? ((a != "") ?a: "τ") : ""
                      return acts;
                    }) ;
//                  textpath.append("tspan")
//                    .attr("class", "fexp")
//                    .style("fill","#0F024F")
//                    .text(function (d) {
//                      var u = d.type.split("~")[2] ;
//                      return (u != "" && u!== undefined) ? ", " + u : "";
//                    });
//                  textpath.append("tspan")
//                    .attr("class", "cresets")
//                    .style("fill","#4C2EAA")
//                    .text(function (d) {
//                      var r = d.type.split("~")[3] ;
////                      return (typeof r != 'undefined') ? (", " + r) : " ";
//                      return (r != "" && r!== undefined) ? ", " + r : "";
//                    });

                  var belowTextpath = d3.select(".labels${name}")
                    .selectAll(".edgelabel")
                    .append('textPath')
                    .call(d3.drag()
                    .on("start", dragstartedAut)
                    .on("drag", draggedAut)
                    .on("end", dragendedAut))
                    .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                    .style("text-anchor", "middle")
                      .style("font-size", "6px")
                    // .style("pointer-events", "none")
                    .attr("startOffset", "50%")
                    .on("mouseenter", function(d) {
                      d3.select(this).style("font-size","14px");})
                    .on("mouseleave", function(d) {
                      d3.select(this).style("font-size", "6px");});

                  belowTextpath.append("tspan")
                    .attr("class", "cc")
                    .style("fill","#00B248")
                    .attr("dy","1em")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      return (g != "" ) ?  g : " ";
                    });
                  belowTextpath.append("tspan")
                    .attr("class", "fexp")
                    .style("fill","#0F024F")
                    .text(function (d) {
                      var cc = d.type.split("~")[0] ;
                      var u = d.type.split("~")[2] ;
                      return (u != "" && u!== undefined) ? ((cc!="") ?", " + u : u) : "";
                    });
                  belowTextpath.append("tspan")
                    .attr("class", "cresets")
                    .style("fill","#4C2EAA")
                    .text(function (d) {
                      var r = d.type.split("~")[3] ;
                      return (r != "" && r!== undefined) ? ", " + r : "";
                    });

            }
          }

          function tickedAut() {
              var node = d3.select(".nodes${name}")
                  .selectAll("circle")
                  .attr('cx', function(d) {return d.x = Math.max(radiusAut, Math.min(widthAut - radiusAut, d.x)); })
                  .attr('cy', function(d) {return d.y = Math.max(radiusAut, Math.min(heightAut - radiusAut, d.y)); });
              var label = d3.select(".nodes${name}")
                  .selectAll("text")
                  .attr('x', function(d) {return d.x = Math.max(radiusAut, Math.min(widthAut - radiusAut, d.x)); })
                  .attr('y', function(d) {return d.y = Math.max(radiusAut, Math.min(heightAut - radiusAut, d.y)); });

              var link = d3.select(".links${name}")
                  .selectAll("polyline")
                  .attr("points", function(d) {
                      return d.source.x + "," + d.source.y + " " +
                      (d.source.x + d.target.x)/2 + "," + (d.source.y + d.target.y)/2 + " " +
                      d.target.x + "," + d.target.y; });
  //                .attr("x1", function(d) { return d.source.x; })
  //                .attr("y1", function(d) { return d.source.y; })
  //                .attr("x2", function(d) { return d.target.x; })
  //                .attr("y2", function(d) { return d.target.y; });
              d3.select(".paths${name}").selectAll(".edgepath").attr('d', function (d) {
                  m = (d.target.y - d.source.y)/(d.target.x - d.source.x);
                  b = d.target.y - m*d.target.x;
                  new_source_x = d.source.x - 2000;
                  new_target_x = d.target.x + 2000;
                  new_source_y = new_source_x * m  +b;
                  new_target_y = new_target_x * m +b;
                  //return 'M ' + new_source_x +' '+ new_source_y  +' L '+ new_target_x +' '+ new_target_y;
                  return 'M ' + new_source_x +' '+ new_source_y  +' L '+ new_target_x +' '+ new_target_y;
              });
              d3.select(".labels${name}").selectAll(".edgelabel").attr('transform', function (d) {
                new_source_x = d.source.x - 2000;
                new_target_x = d.target.x + 2000;
                  if (new_target_x < new_source_x) {
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


          function dragstartedAut(d) {
            if (!d3.event.active) simulationAut.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
          }
          function draggedAut(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
          }
          function dragendedAut(d) {
            if (!d3.event.active) simulationAut.alphaTarget(0);
            if (d.group == 3 || d.group == 1){
              d.fx = null;
              d.fy = null;
            }
          }
      }
      """
  }

  private def getNodes(aut:SBAutomata):String = {
    aut.trans.flatMap(processNode(aut.init,_)).mkString("[",",","]")
  }
  private def processNode(init:SBState,t:SBTrans):Set[String] = {
      val from = t.from.hashCode().toString
      val to = t.to.hashCode().toString
      val id = t.hashCode().toString
    val res = Set(s"""{"id": "$from", "group": ${if (t.from==init) "0" else "1"} ,"inv":"${dsl.backend.Show(t.from)}"}""",
      s"""{"id": "$to", "group": ${if (t.to==init) "0" else "1"} ,"inv":"${dsl.backend.Show(t.to)}"}""",
      s"""{"id": "$from-1-$to-$id", "group": "2","inv":""}""",
      s"""{"id": "$to-2-$from-$id", "group": "2" ,"inv":""}""")
    res
  }
  private def getLinks(aut:SBAutomata,name:String) = {
    aut.trans.flatMap(processEdge(_,name)).mkString("[",",","]")
  }
  private def processEdge(t:SBTrans,name:String):Set[String] = {
      val id = t.hashCode().toString
      val from = t.from.hashCode().toString
      val to = t.to.hashCode().toString
      val lbl = t.label
      Set(s"""{"id": "${id}" , "source": "$from", "target": "$from-1-$to-$id", "type":"", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$from-1-$to-$id", "target": "$to-2-$from-$id", "type":"$lbl", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$to-2-$from-$id", "target": "$to", "type":"", "start":"start", "end": "endarrowout${name}"}""")
  }

  private def getNodes[A<:Automata](aut: A): String =
    aut.getTrans().flatMap(processNode(aut.getInit, _)).mkString("[",",","]")

//  private def getIftaNodes(aut:IftaAutomata):String =
//    aut.getTrans().flatMap(processNode(aut.getInit, _, aut.ifta.cInv)).mkString("[",",","]")

  private def getInvNodes[A<:Automata](aut:A):String = aut match {
    case IftaAutomata(ifta,nifta,cons) =>
      aut.getTrans().flatMap(processNode(aut.getInit,_,ifta.cInv)).mkString("[",",","]")
    case HubAutomata(ps,sts,init,trans,c,inv,v,tp) =>
      aut.getTrans().flatMap(processHNode(aut.getInit,_,inv)).mkString("[",",","]")//todo:temporary fix different fun. Probably better Update ifta to work with CCons
  }



  private def getLinks[A<:Automata](aut: A,name:String,portNames:Boolean=false, ms: Mirrors): String =
    aut.getTrans(portNames).flatMap(t => processEdge(expandMirrors(t,ms),name)).mkString("[",",","]")

  private def processHNode(initAut:Int,trans:(Int,Any,String,Int),nodeInvariant:Map[Int,CCons]=Map()): Set[String] = trans match{
    case (from,lbl,id,to) =>
      val toInv   = hub.backend.Show(hub.backend.Simplify(nodeInvariant.getOrElse(to,hub.CTrue)))
      val fromInv = hub.backend.Show(hub.backend.Simplify(nodeInvariant.getOrElse(from,hub.CTrue)))
      val (gfrom,gto,gp1,gp2) = nodeGroups(initAut,from,to)
      Set(s"""{"id": "$from", "group": $gfrom ,"inv":"${if (fromInv == "true") "" else fromInv}"}""",
        s"""{"id": "$to", "group": $gto ,"inv":"${if (toInv == "true") "" else toInv }"}""",
        s"""{"id": "$from-1-$to-$id", "group": "$gp1","inv":""}""",
        s"""{"id": "$to-2-$from-$id", "group": "$gp2" ,"inv":""}""")
  }

  private def processNode(initAut:Int,trans:(Int,Any,String,Int),nodeInvariant:Map[Int,ClockCons]=Map()): Set[String] = trans match{
    case (from,lbl,id,to) =>
      val toInv   = Show(Simplify(nodeInvariant.getOrElse(to,CTrue)))
      val fromInv = Show(Simplify(nodeInvariant.getOrElse(from,CTrue)))
      val (gfrom,gto,gp1,gp2) = nodeGroups(initAut,from,to)
      Set(s"""{"id": "$from", "group": $gfrom ,"inv":"${if (fromInv == "true") "" else fromInv}"}""",
        s"""{"id": "$to", "group": $gto ,"inv":"${if (toInv == "true") "" else toInv }"}""",
        s"""{"id": "$from-1-$to-$id", "group": "$gp1","inv":""}""",
        s"""{"id": "$to-2-$from-$id", "group": "$gp2" ,"inv":""}""")
  }

  private def expandMirrors(trans: (Int, Any, String, Int), mirrors: Network.Mirrors): (Int,String,String,Int) =
  trans match {
    case (from,lbl,id,to) => (from, expandMirrors(lbl.toString,mirrors),id,to)
  }
  private def expandMirrors(string: String,mirrors: Mirrors): String = {
    val res = string.split("§").toList match {
      case Nil => string
      case head :: tl =>
        val sync = head //.split("~").mkString(" / ") // TODO: uncomment to use Animation from PortAutomata
        val ints = collectInts(tl,mirrors)
//        val expanded = ints.flatMap(mirrors(_))
        (sync::ints.map(_.toString).toList).mkString("~")
    }
    //println(s"- expanding $string --> $res")
    res
  }
  private def collectInts(strings: List[String],mirrors: Mirrors): Set[Int] = {
    strings.toSet.flatMap((s:String) => Try(s.toInt).toOption match {
      case Some(i:Int) => mirrors(i) + i
      case _ => Set[Int]()
    })
  }

  //  private def processNode(initAut:Int,trans:(Int,(Int,Set[Int],Set[Edge]))): Set[String] = trans match{
  //    case (from,(to,fire,es)) =>
  //      val (gfrom,gto,gp1,gp2) = nodeGroups(initAut,from,to)
  //      Set(s"""{"id": "$from", "group": $gfrom }""",
  //          s"""{"id": "$to", "group": $gto }""",
  //          s"""{"id": "$from-1-$to-${fire.mkString(".")}", "group": $gp1}""",
  //          s"""{"id": "$to-2-$from-${fire.mkString(".")}", "group": $gp2 }""")
  //  }

  /**
    * Select the right group:
    *  - 0: initial state
    *  - 1: normal state
    *  - otherwise: connection dot
    */
  private def nodeGroups(initAut:Int,from:Int,to:Int):(String,String,String,String) =
    (   if(from==initAut) "0" else "1"
      , if(to==initAut) "0" else "1"
      , "2" , "2"
    )

  private def processEdge(trans:(Int,String,String,Int), name:String): Set[String] = trans match {
    case (from, lbl,id, to) => {
      Set(s"""{"id": "${id}" , "source": "$from", "target": "$from-1-$to-$id", "type":"", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$from-1-$to-$id", "target": "$to-2-$from-$id", "type":"$lbl", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$to-2-$from-$id", "target": "$to", "type":"", "start":"start", "end": "endarrowout${name}"}""")
    }
  }

}
