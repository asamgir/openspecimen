
angular.module('os.biospecimen.visit.detail', ['os.biospecimen.models'])
  .controller('VisitDetailCtrl', function(
    $scope, $state,
    cpr, visit, specimens, DeleteUtil) {

    function init() {
      $scope.cpr = cpr;
      $scope.visit = visit;
      $scope.specimens = specimens;
    }
          
    function onVisitDeletion() {
      $state.go('participant-detail.overview', {cprId: cpr.id, cpId: cpr.cpId});
    }

    $scope.deleteVisit = function() {
      DeleteUtil.delete($scope.visit, {onDeletion: onVisitDeletion});
    }


    init();
  });
