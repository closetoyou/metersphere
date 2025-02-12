export function getUrl(d) {
  let url = "/#";
  let resourceId = d.sourceId;
  if (resourceId && (resourceId.startsWith("\"") || resourceId.startsWith("["))) {
    resourceId = JSON.parse(d.sourceId);
  }
  if (resourceId instanceof Array) {
    if (resourceId.length === 1) {
      resourceId = resourceId[0];
    } else {
      return url;
    }
  }
  switch (d.type) {
    case "HTTPSamplerProxy":
      switch (d.refType){
        case "API":
          url += "/api/definition?resourceId=" + resourceId;
        break;
        case "CASE":
          url += "/api/definition?caseId=" + d.id;
        break;
      }
      break;
    case "scenario":
      url += "/api/automation?resourceId=" + resourceId;
      break;
    default:
      break;
  }
  return url;
}
