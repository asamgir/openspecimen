
angular.module('os.administrative.user.detail', ['os.administrative.models'])
  .controller('UserDetailCtrl', function($scope, $q, user, Institute, Role, PvManager) {
    $scope.user = user;
    //TODO: remove it after backend code changes  
    $scope.user.userCPRoles = [{site:'AKU', cpTitle:'AASProtocol', roleName:'Administrator'}, 
                               {site:'In Transit', cpTitle:'AAProtocol', roleName:'Administrator'},
                               {site:'In Transit', cpTitle:'AASP', roleName:'Custom'}];
                               
    PvManager.loadPvs($scope, 'domains');
  
    $scope.sites = PvManager.getSites();
    
    Institute.list().then(function(institutes) {
      $scope.institutes = institutes;
    });
        
    $scope.editUser = function(property, value) {
      var d = $q.defer();
      d.resolve({});
      return d.promise;
    }
  });
